package application;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainVictrex2 {
    private List<Country> countries = new ArrayList<>();
    private List<Person> people = new ArrayList<>();

    public MainVictrex2() {

        init();

        File data = new File("C:\\Users\\alex\\Downloads\\data.txt");
        System.out.println(data.exists());

        try (BufferedReader br = new BufferedReader(new FileReader(data))) {
            String line;
            // Get hierarchy stuff
            while ((line = br.readLine()) != null) {
                String[] split = line.split("\t");
                try {
                    people.add(new Person(split[2], split[0], split[7], Double.parseDouble(split[3]), "", Double.parseDouble(split[6]), "", Double.parseDouble(split[5]), Double.parseDouble(split[8])));
                } catch (NumberFormatException e) {
                    System.out.println("NFE: Couldn't do " + line);
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.out.println("Out of Bounds: Couldn't do " + line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Loaded data");

        for (Person person : people) {
            Boolean valueSet = false;
            for (Country country : countries) {
                if (country.getCountryCodes().contains(person.getCountry())) {
                    if (person.getMarket() > 0) {
                        Double ratio = (person.getBaseSalary() / person.getMarket() * 100);

                        Rating rating = country.getRating(person.getRating());
                        if (rating != null) {
                            Double ratingMulti = 0.0;
                            if (ratio < 80.0) {
                                ratingMulti = rating.getLessThan80();
                            } else if (ratio >= 80.0 && ratio < 90.0) {
                                ratingMulti = rating.getPer80to89();
                            } else if (ratio >= 90.0 && ratio < 100.0) {
                                ratingMulti = rating.getPer90to99();
                            } else if (ratio >= 100.0 && ratio < 110.0) {
                                ratingMulti = rating.getPer100to109();
                            } else if (ratio >= 110.0 && ratio < 120.0) {
                                ratingMulti = rating.getPer110to119();
                            } else if (ratio >= 120) {
                                ratingMulti = rating.getMoreThan120();
                            }


                            if (!valueSet) {
                                if (person.getProdRI().equals(ratingMulti)) {
                                    System.out.println(person.getEmplid() + "," + person.getCountry() + "," + person.getRating() + "," + person.getBaseSalary() + "," + person.getMarket() + "," + ((person.getBaseSalary() / person.getMarket()) * 100) + "," + ratingMulti + ",OK (" + person.getProdRI() + ")");
                                } else {
                                    System.out.println(person.getEmplid() + "," + person.getCountry() + "," + person.getRating() + "," + person.getBaseSalary() + "," + person.getMarket() + "," + ((person.getBaseSalary() / person.getMarket()) * 100) + "," + ratingMulti + ",OK - Different! (" + person.getProdRI() + ")");
                                }
                            }
                            valueSet = true;
                        } else {
                            if (!valueSet) {
                                System.out.println(person.getEmplid() + "," + person.getCountry() + "," + person.getRating() + "," + person.getBaseSalary() + "," + person.getMarket() + "," + ((person.getBaseSalary() / person.getMarket()) * 100) + ",-1,Rating not found");
                            }
                            valueSet = true;
                        }
                    } else {
                        if (!valueSet) {
                            System.out.println(person.getEmplid() + "," + person.getCountry() + "," + person.getRating() + "," + person.getBaseSalary() + "," + person.getMarket() + "," + ((person.getBaseSalary() / person.getMarket()) * 100) + ",-1,Has zero market");
                        }
                        valueSet = true;
                    }
                }
            }

            if (!valueSet) {
                System.out.println(person.getEmplid() + "," + person.getCountry() + "," + person.getRating() + "," + person.getBaseSalary() + "," + person.getMarket() + "," + ((person.getBaseSalary() / person.getMarket()) * 100) + ",-1,No country match, or something else");
            }
            valueSet = true;
        }

        System.out.println("Completed");
    }

    public void init() {
        // COUNTRY 1
        Country country1 = new Country();
        country1.addCountryCode("DEU");
        country1.addCountryCode("USA");
        country1.addCountryCode("ITA");
        country1.addCountryCode("AUT");
        country1.addCountryCode("FRA");

        country1.addRating("SEE", new Rating(18.0, 12.0, 9.0, 6.0, 4.5, 3.0));
        country1.addRating("EE", new Rating(12.0, 9.0, 6.0, 4.5, 3.0, 2.0));
        country1.addRating("EFM", new Rating(9.0, 6.0, 4.5, 3.0, 2.0, 1.0));
        country1.addRating("MME", new Rating(2.0, 1.5, 1.0, 1.0, 0.0, 0.0));
        country1.addRating("DNM", new Rating(0.0, 0.0, 0.0, 0.0, 0.0, 0.0));

        countries.add(country1);

        // COUNTRY 2
        Country country2 = new Country();
        country2.addCountryCode("TAI");

        country2.addRating("SEE", new Rating(18.0, 13.5, 9.0, 7.5, 6.0, 4.0));
        country2.addRating("EE", new Rating(13.5, 9.0, 7.5, 6.0, 4.5, 3.0));
        country2.addRating("EFM", new Rating(9.0, 7.5, 6.0, 4.0, 3.0, 2.0));
        country2.addRating("MME", new Rating(3.0, 2.5, 2.0, 2.0, 0.0, 0.0));
        country2.addRating("DNM", new Rating(0.0, 0.0, 0.0, 0.0, 0.0, 0.0));

        countries.add(country2);

        // COUNTRY 3
        Country country3 = new Country();
        country3.addCountryCode("HKG");

        country3.addRating("SEE", new Rating(15.0, 13.0, 11.0, 9.0, 7.5, 4.0));
        country3.addRating("EE", new Rating(13.0, 11.0, 9.0, 7.5, 6.0, 3.0));
        country3.addRating("EFM", new Rating(11.0, 9.0, 7.5, 6.0, 4.0, 2.0));
        country3.addRating("MME", new Rating(3.0, 2.5, 2.0, 2.0, 0.0, 0.0));
        country3.addRating("DNM", new Rating(0.0, 0.0, 0.0, 0.0, 0.0, 0.0));

        countries.add(country3);

        // COUNTRY 4
        Country country4 = new Country();
        country4.addCountryCode("KOR");

        country4.addRating("SEE", new Rating(17.0, 15.0, 13.0, 11.0, 9.5, 5.0));
        country4.addRating("EE", new Rating(15.0, 13.0, 11.0, 9.5, 8.0, 4.0));
        country4.addRating("EFM", new Rating(13.0, 11.0, 9.5, 8.0, 6.0, 3.0));
        country4.addRating("MME", new Rating(4.0, 3.5, 3.0, 3.0, 0.0, 0.0));
        country4.addRating("DNM", new Rating(0.0, 0.0, 0.0, 0.0, 0.0, 0.0));

        countries.add(country4);

        // COUNTRY 5
        Country country5 = new Country();
        country5.addCountryCode("GBR");

        country5.addRating("SEE", new Rating(11.88, 8.91, 5.94, 4.95, 3.30, 2.75));
        country5.addRating("EE", new Rating(7.92, 5.94, 3.96, 3.3, 2.75, 1.8));
        country5.addRating("EFM", new Rating(6.60, 4.95, 3.3, 2.75, 1.8, 1.0));
        country5.addRating("MME", new Rating(2.38, 1.88, 1.38, 1.38, 0.0, 0.0));
        country5.addRating("DNM", new Rating(0.0, 0.0, 0.0, 0.0, 0.0, 0.0));

        countries.add(country5);

        // COUNTRY 6
        Country country6 = new Country();
        country6.addCountryCode("CHN");

        country6.addRating("SEE", new Rating(20.0, 18.0, 17.0, 16.0, 13.0, 8.0));
        country6.addRating("EE", new Rating(18.0, 17.0, 16.0, 13.0, 8.0, 6.0));
        country6.addRating("EFM", new Rating(17.0, 16.0, 11.0, 10.0, 6.0, 4.0));
        country6.addRating("MME", new Rating(7.0, 6.5, 6.0, 5.0, 0.0, 0.0));
        country6.addRating("DNM", new Rating(0.0, 0.0, 0.0, 0.0, 0.0, 0.0));

        countries.add(country6);

        // COUNTRY 7
        Country country7 = new Country();
        country7.addCountryCode("JPN");

        country7.addRating("SEE", new Rating(15.4, 13.0, 10.0, 7.0, 5.3, 3.5));
        country7.addRating("EE", new Rating(11.6, 9.8, 7.5, 5.3, 3.5, 2.2));
        country7.addRating("EFM", new Rating(7.7, 6.5, 5.0, 3.5, 2.2, 1.2));
        country7.addRating("MME", new Rating(2.8, 2.3, 1.8, 1.8, 0.0, 0.0));
        country7.addRating("DNM", new Rating(0.0, 0.0, 0.0, 0.0, 0.0, 0.0));

        countries.add(country7);

        // COUNTRY 8
        Country country8 = new Country();
        country8.addCountryCode("IND");

        country8.addRating("SEE", new Rating(19.0, 17.9, 14.6, 11.2, 9.1, 7.0));
        country8.addRating("EE", new Rating(17.9, 14.6, 11.2, 9.1, 7.0, 4.0));
        country8.addRating("EFM", new Rating(14.6, 11.2, 9.1, 7.0, 4.0, 2.0));
        country8.addRating("MME", new Rating(6.5, 6.0, 5.5, 3.5, 0.0, 0.0));
        country8.addRating("DNM", new Rating(0.0, 0.0, 0.0, 0.0, 0.0, 0.0));

        countries.add(country8);
    }

    public static void main(String[] args) {
        System.out.println("Running");

        new MainVictrex2();
    }

    private class Country {
        private HashMap<String, Rating> ratings = new HashMap<>();
        private List<String> countryCodes = new ArrayList<>();

        public Country() {
        }

        public void addCountryCode(String code) {
            countryCodes.add(code);
        }

        public void addRating(String name, Rating rating) {
            ratings.put(name, rating);
        }

        public Rating getRating(String name) {
            return ratings.get(name);
        }

        public List<String> getCountryCodes() {
            return countryCodes;
        }
    }

    private class Rating {
        private Double lessThan80;
        private Double per80to89;
        private Double per90to99;
        private Double per100to109;
        private Double per110to119;
        private Double moreThan120;

        public Rating(Double lessThan80, Double per80to89, Double per90to99, Double per100to109, Double per110to119, Double moreThan120) {
            this.lessThan80 = lessThan80;
            this.per80to89 = per80to89;
            this.per90to99 = per90to99;
            this.per100to109 = per100to109;
            this.per110to119 = per110to119;
            this.moreThan120 = moreThan120;
        }

        public Double getLessThan80() {
            return lessThan80;
        }

        public Double getPer80to89() {
            return per80to89;
        }

        public Double getPer90to99() {
            return per90to99;
        }

        public Double getPer100to109() {
            return per100to109;
        }

        public Double getPer110to119() {
            return per110to119;
        }

        public Double getMoreThan120() {
            return moreThan120;
        }
    }

    private class Person {
        private String country;
        private String emplid;
        private String rating;
        private Double baseSalary;
        private String baseCurr;
        private Double market;
        private String marketCurr;
        private Double prodRI;
        private Double fte;

        public Person(String country, String emplid, String rating, Double baseSalary, String baseCurr, Double market, String marketCurr, Double prodRI, Double fte) {
            this.country = country;
            this.emplid = emplid;
            this.rating = rating;
            this.baseSalary = baseSalary;
            this.baseCurr = baseCurr;
            this.market = market;
            this.marketCurr = marketCurr;
            this.prodRI = prodRI;
            this.fte = fte;
        }

        public String getCountry() {
            return country;
        }

        public String getEmplid() {
            return emplid;
        }

        public String getRating() {
            return rating;
        }

        public Double getBaseSalary() {
            return baseSalary / fte;
        }

        public String getBaseCurr() {
            return baseCurr;
        }

        public Double getMarket() {
            return market;
        }

        public String getMarketCurr() {
            return marketCurr;
        }

        public Double getProdRI() {
            return prodRI;
        }

        public Double getFte() {
            return fte;
        }
    }
}
