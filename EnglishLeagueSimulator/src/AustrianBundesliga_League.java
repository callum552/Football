import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The main class for the Austrian Bundesliga Simulator.
 * This version simulates the 12-team league, the unique post-season split with
 * points halving, a simplified ÖFB-Cup, and features accurate head-to-head tie-breaker rules.
 *
 * NOTE: This class has been refactored to work with the EuropeanCompetitionSimulator.
 */
public class AustrianBundesliga_League {
    private final List<Team> teams;
    private final List<Match> regularSeasonFixtures;
    private final List<Match> playoffFixtures;
    private final MatchSimulator matchSimulator;
    private final Random random = new Random();

    private Team ofbCupWinner;
    private List<Team> championshipRoundTeams;
    private List<Team> relegationRoundTeams;

    // Lists to store qualified teams for European competitions
    private final List<Team> uclTeams = new ArrayList<>();
    private final List<Team> uelTeams = new ArrayList<>();
    private final List<Team> ueclTeams = new ArrayList<>();

    // Helper class for Head-to-Head stats
    private static class HeadToHeadStats {
        int points = 0;
        int goalsFor = 0;
        int goalsAgainst = 0;
        int goalDifference = 0;
    }

    public AustrianBundesliga_League() {
        this.teams = new ArrayList<>();
        this.regularSeasonFixtures = new ArrayList<>();
        this.playoffFixtures = new ArrayList<>();
        this.matchSimulator = new MatchSimulator();
    }

    // --- GETTERS FOR EUROPEAN QUALIFIERS ---
    public List<Team> getUclTeams() { return uclTeams; }
    public List<Team> getUelTeams() { return uelTeams; }
    public List<Team> getUeclTeams() { return ueclTeams; }


    public static void main(String[] args) {
        AustrianBundesliga_League austria = new AustrianBundesliga_League();

        System.out.println("--- Setting up Austrian Bundesliga with real teams ---");
        austria.setupTeams();

        System.out.println("\n--- Simulating the ÖFB-Cup... ---");
        austria.simulateOFBCup();

        System.out.println("\n--- Generating Regular Season Fixtures ---");
        austria.generateRegularSeasonFixtures();

        System.out.println("\n--- Simulating Regular Season... ---");
        austria.simulateRegularSeason();

        System.out.println("\n--- REGULAR SEASON TABLE (END OF 22 MATCHES) ---");
        austria.displayTable("REGULAR SEASON TABLE", false);

        System.out.println("\n--- Performing Play-off Split and Generating Play-off Fixtures... ---");
        austria.performPlayoffSplit();

        System.out.println("\n--- Simulating Play-offs... ---");
        austria.simulatePlayoffs();

        System.out.println("\n--- Determining European Spots ---");
        austria.determineEuropeanSpots();

        System.out.println("\n--- FINAL AUSTRIAN BUNDESLIGA TABLES ---");
        austria.displayTable("FINAL TABLES", true);
    }

    public void setupTeams() {
        double initialElo = 1500;
        this.teams.add(new Team("RB Salzburg", 96, 85, initialElo + 350));
        this.teams.add(new Team("Sturm Graz", 85, 80, initialElo + 180));
        this.teams.add(new Team("LASK", 82, 79, initialElo + 150));
        this.teams.add(new Team("Rapid Vienna", 80, 78, initialElo + 120));
        this.teams.add(new Team("Austria Vienna", 78, 77, initialElo + 80));
        this.teams.add(new Team("Wolfsberger AC", 76, 75, initialElo + 40));
        this.teams.add(new Team("Hartberg", 74, 72, initialElo));
        this.teams.add(new Team("Austria Klagenfurt", 72, 74, initialElo - 50));
        this.teams.add(new Team("Blau-Weiss Linz", 70, 71, initialElo - 100));
        this.teams.add(new Team("Rheindorf Altach", 68, 73, initialElo - 150));
        this.teams.add(new Team("WSG Tirol", 67, 70, initialElo - 200));
        this.teams.add(new Team("Austria Lustenau", 65, 68, initialElo - 250));
        System.out.println("12 Austrian Bundesliga teams have been created.");
    }

    public void simulateOFBCup() {
        List<Team> cupTeams = new ArrayList<>(this.teams);
        cupTeams.sort(Comparator.comparingDouble(Team::getEloRating).reversed());
        List<Team> preliminaryTeams = new ArrayList<>(cupTeams.subList(4, 12));
        List<Team> teamsWithByes = new ArrayList<>(cupTeams.subList(0, 4));

        System.out.println("\n** ÖFB-Cup Preliminary Round **");
        List<Team> preliminaryWinners = new ArrayList<>();
        for (int i = 0; i < preliminaryTeams.size(); i += 2) {
            preliminaryWinners.add(matchSimulator.simulateSingleMatch(preliminaryTeams.get(i), preliminaryTeams.get(i + 1)));
        }

        List<Team> quarterFinalists = new ArrayList<>(teamsWithByes);
        quarterFinalists.addAll(preliminaryWinners);

        System.out.println("\n** ÖFB-Cup Quarter-Finals **");
        List<Team> semiFinalists = simulateKnockoutRound(quarterFinalists);
        System.out.println("\n** ÖFB-Cup Semi-Finals **");
        List<Team> finalists = simulateKnockoutRound(semiFinalists);
        System.out.println("\n** ÖFB-Cup Final **");
        this.ofbCupWinner = matchSimulator.simulateSingleMatch(finalists.get(0), finalists.get(1));
        System.out.println("\nÖFB-Cup Winner: " + this.ofbCupWinner.getName());
    }

    private List<Team> simulateKnockoutRound(List<Team> teamsInRound) {
        List<Team> winners = new ArrayList<>();
        Collections.shuffle(teamsInRound, random);
        for (int i = 0; i < teamsInRound.size(); i += 2) {
            winners.add(matchSimulator.simulateSingleMatch(teamsInRound.get(i), teamsInRound.get(i + 1)));
        }
        return winners;
    }

    public void generateRegularSeasonFixtures() {
        for (Team team1 : this.teams) {
            for (Team team2 : this.teams) {
                if (!team1.equals(team2)) {
                    regularSeasonFixtures.add(new Match(team1, team2));
                }
            }
        }
        System.out.println(regularSeasonFixtures.size() + " regular season matches scheduled.");
    }

    public void simulateRegularSeason() {
        for (int i = 0; i < regularSeasonFixtures.size(); i++) {
            Match updatedMatch = matchSimulator.simulateMatch(regularSeasonFixtures.get(i));
            regularSeasonFixtures.set(i, updatedMatch);
        }
    }

    public void performPlayoffSplit() {
        this.teams.sort(Comparator.comparingInt(Team::getPoints).reversed());

        championshipRoundTeams = new ArrayList<>(this.teams.subList(0, 6));
        relegationRoundTeams = new ArrayList<>(this.teams.subList(6, 12));

        for(Team team : this.teams) {
            team.points = (int) Math.ceil(team.points / 2.0);
        }

        addPlayoffFixtures(championshipRoundTeams, this.playoffFixtures);
        addPlayoffFixtures(relegationRoundTeams, this.playoffFixtures);
        System.out.println("Play-off groups created and points halved.");
    }

    private void addPlayoffFixtures(List<Team> section, List<Match> fixtureList) {
        for (Team team1 : section) {
            for (Team team2 : section) {
                if (!team1.equals(team2)) {
                    fixtureList.add(new Match(team1, team2));
                }
            }
        }
    }

    public void simulatePlayoffs() {
        for (int i = 0; i < playoffFixtures.size(); i++) {
            Match updatedMatch = matchSimulator.simulateMatch(playoffFixtures.get(i));
            playoffFixtures.set(i, updatedMatch);
        }
    }

    public void determineEuropeanSpots() {
        // Sort teams into final order first
        championshipRoundTeams.sort(getFinalTableSorter());
        relegationRoundTeams.sort(getFinalTableSorter());

        List<Team> finalOrder = new ArrayList<>();
        finalOrder.addAll(championshipRoundTeams);
        finalOrder.addAll(relegationRoundTeams);

        Set<Team> qualifiedForEurope = new HashSet<>();

        // 1. Champions League
        uclTeams.add(finalOrder.get(0)); // [UCL] Champion
        uclTeams.add(finalOrder.get(1)); // [UCL-Q] Runner-up
        qualifiedForEurope.addAll(uclTeams);

        // 2. Europa League
        // Cup winner gets UEL Play-off spot
        if (!qualifiedForEurope.contains(ofbCupWinner)) {
            uelTeams.add(ofbCupWinner); // [UEL-PO]
            qualifiedForEurope.add(ofbCupWinner);
        }

        // 3. Conference League
        int leagueSpotCounter = 2; // Start from 3rd place

        // If cup winner is already in UCL, UEL spot from cup is passed down to 3rd place
        if(qualifiedForEurope.contains(ofbCupWinner) || uelTeams.isEmpty()){
            while(uelTeams.size() < 1 && leagueSpotCounter < finalOrder.size()){
                Team team = finalOrder.get(leagueSpotCounter);
                if(!qualifiedForEurope.contains(team)){
                    uelTeams.add(team); // [UEL-Q]
                    qualifiedForEurope.add(team);
                }
                leagueSpotCounter++;
            }
        }

        // Next two available spots get UECL-Q
        while(ueclTeams.size() < 2 && leagueSpotCounter < finalOrder.size()){
            Team team = finalOrder.get(leagueSpotCounter);
            if(!qualifiedForEurope.contains(team)){
                ueclTeams.add(team); // [UECL-Q]
                qualifiedForEurope.add(team);
            }
            leagueSpotCounter++;
        }
    }


    public void displayTable(String title, boolean isFinal) {
        System.out.println("\n--- " + title + " ---");

        Comparator<Team> tableSorter = getFinalTableSorter();

        if (isFinal) {
            championshipRoundTeams.sort(tableSorter);
            relegationRoundTeams.sort(tableSorter);
            System.out.println("\n** CHAMPIONSHIP ROUND **");
            printTableSection(championshipRoundTeams, 1, true);
            System.out.println("\n** RELEGATION ROUND **");
            printTableSection(relegationRoundTeams, 7, true);
        } else {
            List<Team> regularSeasonOrder = new ArrayList<>(this.teams);
            regularSeasonOrder.sort(tableSorter);
            printTableSection(regularSeasonOrder, 1, false);
        }
    }

    private Comparator<Team> getFinalTableSorter() {
        List<Match> allMatches = new ArrayList<>(regularSeasonFixtures);
        allMatches.addAll(playoffFixtures);

        return (t1, t2) -> {
            if (t1.getPoints() != t2.getPoints()) return Integer.compare(t2.getPoints(), t1.getPoints());

            List<Team> tiedGroup = this.teams.stream().filter(t -> t.getPoints() == t1.getPoints()).collect(Collectors.toList());
            if (tiedGroup.size() > 1) {
                Map<Team, HeadToHeadStats> h2hStats = new HashMap<>();
                for (Team t : tiedGroup) h2hStats.put(t, new HeadToHeadStats());

                for (Match m : allMatches) {
                    if (m.homeGoals != -1 && tiedGroup.contains(m.homeTeam) && tiedGroup.contains(m.awayTeam)) {
                        HeadToHeadStats homeStats = h2hStats.get(m.homeTeam);
                        HeadToHeadStats awayStats = h2hStats.get(m.awayTeam);
                        homeStats.goalsFor += m.homeGoals;
                        homeStats.goalsAgainst += m.awayGoals;
                        awayStats.goalsFor += m.awayGoals;
                        awayStats.goalsAgainst += m.homeGoals;
                        if (m.homeGoals > m.awayGoals) homeStats.points += 3;
                        else if (m.homeGoals == m.awayGoals) { homeStats.points += 1; awayStats.points += 1; }
                        else { awayStats.points += 3; }
                    }
                }
                HeadToHeadStats stats1 = h2hStats.get(t1);
                HeadToHeadStats stats2 = h2hStats.get(t2);
                if (stats1.points != stats2.points) return Integer.compare(stats2.points, stats1.points);

                stats1.goalDifference = stats1.goalsFor - stats1.goalsAgainst;
                stats2.goalDifference = stats2.goalsFor - stats2.goalsAgainst;
                if (stats1.goalDifference != stats2.goalDifference) return Integer.compare(stats2.goalDifference, stats1.goalDifference);
                if(stats1.goalsFor != stats2.goalsFor) return Integer.compare(stats2.goalsFor, stats1.goalsFor);
            }
            if (t1.getGoalDifference() != t2.getGoalDifference()) return Integer.compare(t2.getGoalDifference(), t1.getGoalDifference());
            if (t1.getGoalsFor() != t2.getGoalsFor()) return Integer.compare(t2.getGoalsFor(), t1.getGoalsFor());
            if(t1.getWins() != t2.getWins()) return Integer.compare(t2.getWins(), t1.getWins());

            return t1.getName().compareTo(t2.getName());
        };
    }

    private void printTableSection(List<Team> sectionTeams, int startPosition, boolean isFinal) {
        System.out.println("Pos | Team                     | P  | W  | D  | L  | GF | GA | GD  | Pts | Elo ");
        System.out.println("------------------------------------------------------------------------------------");

        int position = startPosition;
        for (Team team : sectionTeams) {
            String teamDisplayName = team.getName();
            String qualificationMarker = "";

            if (isFinal) {
                if(uclTeams.contains(team)){
                    qualificationMarker = position == 1 ? " [C][UCL]" : " [UCL-Q]";
                } else if (uelTeams.contains(team)){
                    qualificationMarker = ofbCupWinner.equals(team) ? " [UEL-PO]" : " [UEL-Q]";
                } else if (ueclTeams.contains(team)){
                    qualificationMarker = " [UECL-Q]";
                }

                if (position == 12) qualificationMarker += " [R]";
            }

            teamDisplayName += qualificationMarker.trim();

            System.out.printf("%-3d | %-26s | %-2d | %-2d | %-2d | %-2d | %-2d | %-2d | %-3d | %-3d | %.0f%n",
                    position++,
                    teamDisplayName,
                    team.gamesPlayed,
                    team.wins,
                    team.draws,
                    team.losses,
                    team.goalsFor,
                    team.goalsAgainst,
                    team.goalDifference,
                    team.getPoints(),
                    team.getEloRating());
        }

        if (isFinal && startPosition == 7) {
            System.out.println("------------------------------------------------------------------------------------");
            System.out.println("Legend: [C] Champions, [UCL] Champions League, [UCL-Q] UCL Qualifiers, [UEL-PO] UEL Play-off, [UEL-Q] UEL Qualifiers");
            System.out.println("        [UECL-Q] UECL Qualifiers, [R] Relegation");
            System.out.println("Cup Winner: [ÖFB-Cup: " + this.ofbCupWinner.getName() + "]");
        }
    }
}