package weather;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.json.JSONObject;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 *
 * @author David
 */
public class Weather {
    private final String USER_AGENT = "Mozilla/5.0";
    private static ArrayList<Movie> movies;
    private static ArrayList<Theater> theaters;
    private static ArrayList<JSONObject> jsonTheaters, jsonMovies;
    public static void main(String[] args) throws Exception {

                Weather http = new Weather();
                movies = new ArrayList(); theaters = new ArrayList();
		//System.out.println("Testing 1 - Send Http GET request");
		http.getCityFromZip("33101");
                http.calcDistanceFrom("11201");
                http.getURLS();
                http.getFromIMDB();
                
                for (int i = 0; i < movies.size(); i++) {
                    String year = movies.get(i).getReleaseDate().substring(movies.get(i).getReleaseDate().length()-4,movies.get(i).getReleaseDate().length());
                    
                    if ("Unknown Date".equals(movies.get(i).getReleaseDate()) 
                            || !(year.equals("2015")) 
                            || movies.get(i).getImageUrl().equals("http://upload.wikimedia.org/wikipedia/commons/a/ac/No_image_available.svg")) {
                        //System.out.println("Removed movie " + movies.get(i).getName() + " in year " + movies.get(i).getReleaseDate());
                        for (int e = 0; e < theaters.size(); e++) {
                            for (int p = 0; p < theaters.get(e).movies.size(); p++) {
                                if (theaters.get(e).getMovies().get(p).equals(movies.get(i).getName())) {
                                    //System.out.println("Removed movie: " + movies.get(i).getName() + " from theater " + theaters.get(e).getName());
                                    theaters.get(e).getMovies().remove(p);
                                    break;  
                                }
                            }
                        }
                        movies.remove(i);            
                        i--;
                    } else {
                        movies.get(i).printInfo();
                        //System.out.println();
                    }
                    
                }
       // Fill in trailers         
                http.findTrailersFromYoutube();
                
        jsonTheaters = new ArrayList<JSONObject>();
        
            try {
			File file = new File("moviesplustheaters.txt");
 
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}
 
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
                        
                        for (Theater theater : theaters) {        
                            JSONObject jsonObj = new JSONObject();
                            jsonObj.put("Name", theater.getName());
                            jsonObj.put("Address", theater.getAddress());
                            jsonObj.put("Phone", "212-123-3212");
                            jsonObj.put("Longitude", theater.getLatitude());
                            jsonObj.put("Latitude", theater.getLatitude());
                            jsonObj.put("movies", theater.getMovies());
                            jsonTheaters.add(jsonObj);
                            System.out.println(jsonObj);
                            
                            bw.write(jsonObj.toString());
                            bw.newLine();
                        }
                        for (Movie movie: movies) {
                            JSONObject movieObj = new JSONObject();
                            movieObj.put("Title", movie.getName());
                            movieObj.put("ReleaseDate", movie.getReleaseDate());
                            movieObj.put("Rating", movie.getRating());
                            movieObj.put("Synopsis", movie.getSummary());
                            movieObj.put("Poster", movie.getImageUrl());
                            movieObj.put("Trailer", movie.getTrailer());
                            movieObj.put("Actors", movie.getActors());
                            movieObj.put("Genre", movie.getGenres());
                            System.out.println(movieObj);
                            bw.write(movieObj.toString());
                            bw.newLine();
                        }
                        
                        bw.close();
            } catch (IOException e) {
			e.printStackTrace();
            }
	
     }
    public void findTrailersFromYoutube() throws Exception {
        for (int i = 0; i < movies.size(); i++) {
            String title = movies.get(i).getName();
            title = title.replaceAll(" ", "+");
            
            String url = "https://www.bing.com/search?q=" + title + "+2015+trailer";
           // System.out.println(url);
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", USER_AGENT);
            StringBuffer response;
            try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                String inputLine;
                response = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
            }
            int occurence = 0;
            int occurence2 = 0;
            
            occurence = response.indexOf("youtube.com/watch?v=");
            occurence2 = response.indexOf("\"", occurence);
            if(occurence == -1) {
                
            } else {
                String link = response.substring(occurence+20, occurence2);
                //System.out.println(link);
                
                movies.get(i).setTrailer(link);
            }
        
        }
    }
     //GETS MOVIE INFO FROM IMDB PAGE SUCH AS TITLE, DATE, ACTORS, SUMMARY
    public void getFromIMDB() throws Exception {
        for (int i = 0; i < movies.size(); i++) {
            String url = movies.get(i).getUrl();
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", USER_AGENT);
            StringBuffer response;
            try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                String inputLine;
                response = new StringBuffer();
                ////System.out.println("exception123");
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                    //  //System.out.println(inputLine);
                }
            }
            int occurence = 0;
            int lastOccurence = 0;
            int occurence2 = 0;
            
            //ACTORS
            occurence = response.indexOf("<h4 class=\"inline\">Stars:</h4>");
            occurence2 = response.indexOf("inline nobr\">", occurence);
            if (occurence < 0 || occurence2 < 0 || occurence2 < occurence) {
                ArrayList noActors = new ArrayList();
                noActors.add("Unknown Actors");
                movies.get(i).setActors(noActors);
            } else {    
                String actorSection = response.substring(occurence, occurence2);
                occurence = 0; occurence2 = 0;
                ArrayList actors = new ArrayList();
                while ((occurence = actorSection.indexOf("name\">", lastOccurence)) != -1) {
                    //occurence = response.indexOf("<title>", lastOccurence);
                    occurence2 = actorSection.indexOf("</span></a>", occurence);

                    String actor = actorSection.substring(occurence+6, occurence2);
                    actors.add(actor);
                    lastOccurence = occurence2;
                    occurence = occurence2;

                }
                movies.get(i).setActors(actors);
            }
            
            
            //POSTER URL
            occurence2 = response.indexOf("\"itemprop=\"image\" />");
            occurence = response.indexOf("Poster\"src=", occurence2-300);
            if (occurence < 0 || occurence2 < 0 || occurence2 < occurence) {
                movies.get(i).setImageUrl("http://upload.wikimedia.org/wikipedia/commons/a/ac/No_image_available.svg");
                
            }
            else {
                String imgUrl = response.substring(occurence+12, occurence2);
                movies.get(i).setImageUrl(imgUrl);
            }
            
            
            //SUMMARY
            occurence = response.indexOf("<p itemprop=\"description\">") + 26;
            occurence2 = response.indexOf("</p>", occurence-25);
            String summary = response.substring(occurence, occurence2);
            if (occurence < 0 || occurence2 < 0|| occurence2 < occurence || summary.contains("Add a Plot") || summary.length() > 1000 ) {
                movies.get(i).setSummary("No summary available");
            } else {
                if (summary.contains("...")) {
                    summary = summary.substring(0, summary.indexOf("...")+3);
                }
                movies.get(i).setSummary(summary);
            }
            //RELEASE DATES
            occurence = response.indexOf("See all release dates");
            occurence2 = response.indexOf("<meta itemprop", occurence);
                
            if (occurence < 0 || occurence2 < 0 || occurence2 < occurence) {
                movies.get(i).setReleaseDate("Unknown Date");
            } else {
                String releaseDate = response.substring(occurence+25, occurence2);
                movies.get(i).setReleaseDate(releaseDate);
            }

            //rating
            occurence = response.indexOf(">Rated");
            occurence2 = response.indexOf(" for", occurence);
            if (occurence < 0 || occurence2 < 0 || occurence2 < occurence) {
                movies.get(i).setRating("Not yet rated");  
            } else {
                String rating = response.substring(occurence+1, occurence2);
                movies.get(i).setRating(rating);
                
            }
            occurence2 = 1;
            ArrayList genres = new ArrayList();
            //genre
            while (1 != 2) {
                occurence = response.indexOf("?ref_=tt_stry_gnr\"", occurence2);
                if (occurence == -1) break;
                occurence2 = response.indexOf("</a>", occurence);
                
                if (occurence < 0 || occurence2 < 0 || occurence2 < occurence) {

                } else {
                    String genre = response.substring(occurence+20, occurence2);
                    genres.add(genre);
                }
            }
            movies.get(i).setGenres(genres);
//            System.out.println(movies.get(i).getName());
//            System.out.println(movies.get(i).getGenres());
        } 
        
    }
    //GETS MOVIE IMDB URL BY SEARCHING BING
    public void getURLS() throws Exception {
        for (int i = 0; i < movies.size(); i++) {
            String title = movies.get(i).getName();
            title = title.replaceAll(" ", "+");
            
            String url = "http://www.bing.com/search?q=" + title + "+imdb";
            URL obj = new URL(url);
            //System.out.println(obj);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", USER_AGENT);    
      
            BufferedReader in = new BufferedReader(
                            new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();    

            while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
            } in.close();           
            int occurence = 0;
         
            occurence = response.indexOf("http://www.imdb.com/title/");       
            if (occurence != -1) {
                movies.get(i).setUrl(response.substring(occurence, occurence+35));
                //System.out.println(movies.get(i).getUrl());
            }
            else {
                movies.remove(i);
                i--;
            }
            ////System.out.println("movie count: " + i);
        }


    }
    //GETS THEATERS BASED ON ZIPCODE, ADDRESS, MOVIES PLAYING
    public void getCityFromZip(String zip) throws Exception {
   
        String url = "http://www.fandango.com/rss/moviesnearme_"+ zip + ".rss";
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", USER_AGENT);
        int responseCode = con.getResponseCode();
        ////System.out.println("Theaters from zip: " + zip);
        BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();    
        ////System.out.println("exception123");
        while ((inputLine = in.readLine()) != null) {
		response.append(inputLine);
              //  //System.out.println(inputLine);
        }
        in.close();
        ArrayList theaters = new ArrayList();
        ArrayList addresses = new ArrayList();
        int occurence = 0;
        int lastOccurence = 0;
        int occurence2 = 0;
        //theaters names
        while ((occurence = response.indexOf("<title>", lastOccurence)) != -1) {
            //occurence = response.indexOf("<title>", lastOccurence);
            occurence2 = response.indexOf("</title>", occurence);
            theaters.add(response.substring(occurence+16, occurence2-3));
            lastOccurence = occurence2;
            occurence = occurence2;
        }
        //theater addresses
        occurence = 0; lastOccurence = 0; occurence2 = 0;
        while ((occurence = response.indexOf("<![CDATA[<p>", lastOccurence)) != -1) {
            //occurence = response.indexOf("<title>", lastOccurence);
            occurence2 = response.indexOf("</p>", occurence);
            addresses.add(response.substring(occurence+12, occurence2));
            
            lastOccurence = occurence2;
            occurence = occurence2;
            
        }
        
        //movies per theater
        ArrayList moviesPlaying[] = new ArrayList[theaters.size()];
        for (int i = 0; i < theaters.size(); i++) {
            moviesPlaying[i] = new ArrayList();
        }
        occurence = 0; lastOccurence = 0; occurence2 = 0;
        occurence = response.indexOf("<description>", lastOccurence);
        lastOccurence = occurence; occurence2 = occurence;
        int theaterCount = 0;
        while ((occurence = response.indexOf("<description><", lastOccurence)) != -1) {
            occurence2 = response.indexOf("</description>", occurence);
            String movieSection = response.substring(occurence, occurence2);
            int movieOccurence = 0; int movieOccurence2 = 0;  int lastOccurence2 = 0;
            boolean exists = false;
            while ((movieOccurence = movieSection.indexOf("\">", lastOccurence2)) != -1) {
                movieOccurence2 = movieSection.indexOf("</a>", movieOccurence);
                String movieTitle = movieSection.substring(movieOccurence+2, movieOccurence2);
                if (movieTitle.contains("Search other theaters")) {break;} else {
                    
                
                    moviesPlaying[theaterCount].add(movieTitle);
                    exists = false;

                    for (int j = 0; j < movies.size(); j++) {
                        if (movies.get(j).getName().equals(movieTitle)) {
                            exists = true;
                        }
                    }
                    if (exists == false) {
                        Movie movie = new Movie(movieTitle);
                        movies.add(movie);
                    }
                    lastOccurence2 = movieOccurence2;
                    movieOccurence = movieOccurence2;
                }
            }
                theaterCount += 1;
                lastOccurence = occurence2;
                occurence = occurence2;
                
        }
        
        //print info
        for (int i = 2; i < theaters.size(); i++) {
            
//            //System.out.println(addresses.get(i-2));
//            for (int a = 0; a < moviesPlaying[i-2].size(); a++) {
//                //System.out.println(moviesPlaying[i-2].get(a));
//            }
            if (!(moviesPlaying[i-2].isEmpty())) {
                Theater theater = new Theater(theaters.get(i).toString());
                theater.setAddress(addresses.get(i-2).toString());
                theater.setMovies(moviesPlaying[i-2]);
                //System.out.println(theater.getName());
                //System.out.println(theater.getAddress());
                //System.out.println(theater.getMovies());
                //System.out.println("theater name:" + theater.getName());
                //latitude and longitude
                setLatLong(theater);
               
                this.theaters.add(theater);
            }
        }
        
        //for (int i =0 ; i< movies.size(); i ++)
        ////System.out.println(movies.get(i).getName());     
    }
    public void setLatLong(Theater t) throws Exception {
        //http://www.bing.com/search?q=10013+latitude+and+longitude
            String address = t.getAddress();
            System.out.println("addresses :" + address);
                    
                    
            String zip = address.substring(address.length()-5, address.length());
          
            double[] latlong = findLatLong(zip);
            t.setLatitude(latlong[0]);
            t.setLongitude(latlong[1]);
            
            //System.out.println(t.latitude + " " + t.longitude);
    }
    public double[] findLatLong(String zip) throws Exception {
            String url = "http://www.zip-info.com/cgi-local/zipsrch.exe?ll=ll&zip=" + zip + "&Go=Go";
            System.out.println("ZIPCODE:" + url);
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", USER_AGENT);
            StringBuffer response;
            try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                String inputLine;
                response = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
            }
            ////System.out.println(response);
            int occurence = 0;
            String searchFor = "</font></td><td align=center>";
            double latitude = 0;
            double longitude = 0;
                occurence = response.lastIndexOf(searchFor);
                //System.out.println(response);
                longitude = Double.parseDouble(response.substring(occurence+searchFor.length(), occurence+searchFor.length()+5));
                
                latitude = Double.parseDouble(response.substring(occurence-7, occurence-2));
           
               
            System.out.println("latitude: " + latitude + ", longitude: " + longitude);
            double[] array = new double[2];
            array[0] = latitude;
            array[1] = longitude;
            return array;
    }
    public void calcDistanceFrom(String zip) throws Exception {
        double[] base = findLatLong(zip);
        
        double distance = 0;
        for (int i = 0; i < theaters.size() ; i ++) {
            Theater t = theaters.get(i);
            distance = distance(base[0], base[1], theaters.get(i).getLatitude(), theaters.get(i).getLongitude(), 'm');
            
            System.out.println("Distance from " + zip + " to " + t.getName() + " is " + distance + " miles.");
        }
    }
    
    
    private double distance(double lat1, double lon1, double lat2, double lon2, char unit) {
      double theta = lon1 - lon2;
      double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
      dist = Math.acos(dist);
      dist = rad2deg(dist);
      dist = dist * 60 * 1.1515;
      if (unit == 'K') {
        dist = dist * 1.609344;
      } else if (unit == 'N') {
        dist = dist * 0.8684;
        }
      return (dist);
    }

    private double deg2rad(double deg) {
      return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
      return (rad * 180.0 / Math.PI);
    }
}

    class Theater {
        ArrayList movies;
        String name, address;
        double latitude, longitude;
        Theater(String name) {
            this.name = name;
            latitude = 0; longitude = 0;
        }

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }

        public ArrayList getMovies() {
            return movies;
        }

        public void setMovies(ArrayList movies) {
            this.movies = movies;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

    }
    class Movie {
        String name, theater, address, url, imageUrl, summary, releaseDate, rating, trailer;
        ArrayList genres;
        ArrayList actors;
        Movie(String name) {
                 this.name = name;
        }
        public void printInfo() {
            //System.out.println("Title: " + name);
            //System.out.println("IMDBUrl: " + url);
            //System.out.println("ImageUrl: " + imageUrl);
            //System.out.println("Summary: " + summary);
            //System.out.println("Rating: " + rating);
            //System.out.println("Cast " + actors.toString());
            //System.out.println("Date: " + releaseDate);
            //System.out.println("Trailer: " + trailer);
            //System.out.println("");
        }
        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public ArrayList getGenres() {
            return genres;
        }

        public void setGenres(ArrayList genres) {
            this.genres = genres;
        }
        

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public String getReleaseDate() {
            return releaseDate;
        }

        public void setReleaseDate(String releaseDate) {
            this.releaseDate = releaseDate;
        }

        public String getRating() {
            return rating;
        }

        public void setRating(String rating) {
            this.rating = rating;
        }

        public String getTrailer() {
            return trailer;
        }

        public void setTrailer(String trailer) {
            this.trailer = trailer;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public ArrayList getActors() {
            return actors;
        }

        public void setActors(ArrayList actors) {
            this.actors = actors;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getTheater() {
            return theater;
        }

        public void setTheater(String theater) {
            this.theater = theater;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }
        
    }    