import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The main class for the Belgian Pro League Simulator.
 * This version simulates the 16-team league, the complex post-season play-offs,
 * a simple Belgian Cup, and features accurate European/relegation rules.
 *
 * NOTE: This class has been refactored to work with the EuropeanCompetitionSimulator.
 */
public class BelgianProLeague_League {
    private final List<Team> teams;
    private final List<Match> phase1Fixtures;
    private final List<Match> championsPlayoffFixtures;
    private final List<Match> europePlayoffFixtures;
    private final MatchSimulator matchSimulator;
    private final Random random = new Random();

    private Team belgianCupWinner;
    private List<Team> championsPlayoffTeams;
    private List<Team> europePlayoffTeams;
    private List<Team> relegationPlayoffTeams;

    // Lists to store qualified teams
    private final List<Team> uclTeams = new ArrayList<>();
    private final List<Team> uelTeams = new ArrayList<>();
    private final List<Team> ueclTeams = new ArrayList<>();


    public BelgianProLeague_League() {
        this.teams = new ArrayList<>();
        this.phase1Fixtures = new ArrayList<>();
        this.championsPlayoffFixtures = new ArrayList<>();
        this.europePlayoffFixtures = new ArrayList<>();
        this.matchSimulator = new MatchSimulator();
    }

    // --- GETTERS FOR EUROPEAN QUALIFIERS ---
    public List<Team> getUclTeams() { return uclTeams; }
    public List<Team> getUelTeams() { return uelTeams; }
    public List<Team> getUeclTeams() { return ueclTeams; }


    public static void main(String[] args) {
        BelgianProLeague_League belgium = new BelgianProLeague_League();

        System.out.println("--- Setting up Belgian Pro League with real teams ---");
        belgium.setupTeams();

        System.out.println("\n--- Simulating the Belgian Cup... ---");
        belgium.simulateBelgianCup();

        System.out.println("\n--- Generating Regular Season Fixtures ---");
        belgium.generateRegularSeasonFixtures();

        System.out.println("\n--- Simulating Regular Season... ---");
        belgium.simulateRegularSeason();

        System.out.println("\n--- REGULAR SEASON TABLE (END OF 30 MATCHES) ---");
        belgium.displayTable("REGULAR SEASON TABLE", false);

        System.out.println("\n--- Performing Play-off Split and Generating Play-off Fixtures... ---");
        belgium.performPlayoffSplit();

        System.out.println("\n--- Simulating Play-offs... ---");
        belgium.simulatePlayoffs();

        System.out.println("\n--- Determining European Spots ---");
        belgium.determineEuropeanSpots();

        System.out.println("\n--- FINAL BELGIAN PRO LEAGUE TABLES ---");
        belgium.displayTable("FINAL TABLES", true);
    }

    public void setupTeams() {
        double initialElo = 1500;
        this.teams.add(new Team("Club Brugge", 88, 82, initialElo + 250));
        this.teams.add(new Team("Anderlecht", 86, 83, initialElo + 220));
        this.teams.add(new Team("Union Saint-Gilloise", 87, 81, initialElo + 210));
        this.teams.add(new Team("KRC Genk", 85, 78, initialElo + 180));
        this.teams.add(new Team("KAA Gent", 84, 79, initialElo + 170));
        this.teams.add(new Team("Antwerp", 82, 80, initialElo + 150));
        this.teams.add(new Team("Cercle Brugge", 78, 77, initialElo + 80));
        this.teams.add(new Team("Standard Liege", 79, 76, initialElo + 50));
        this.teams.add(new Team("Westerlo", 76, 74, initialElo + 20));
        this.teams.add(new Team("Sint-Truiden", 72, 78, initialElo));
        this.teams.add(new Team("Oud-Heverlee Leuven", 74, 73, initialElo - 40));
        this.teams.add(new Team("Charleroi", 73, 75, initialElo - 60));
        this.teams.add(new Team("KV Mechelen", 75, 71, initialElo - 80));
        this.teams.add(new Team("KV Kortrijk", 68, 72, initialElo - 150));
        this.teams.add(new Team("Eupen", 67, 70, initialElo - 180));
        this.teams.add(new Team("RWDM", 65, 68, initialElo - 220));
        System.out.println("16 Belgian Pro League teams have been created.");
    }

    public void simulateBelgianCup() {
        List<Team> cupTeams = new ArrayList<>(this.teams);
        System.out.println("\n** Belgian Cup Round of 16 **");
        List<Team> quarterFinalists = simulateKnockoutRound(cupTeams);
        System.out.println("\n** Belgian Cup Quarter-Finals **");
        List<Team> semiFinalists = simulateKnockoutRound(quarterFinalists);
        System.out.println("\n** Belgian Cup Semi-Finals **");
        List<Team> finalists = simulateKnockoutRound(semiFinalists);
        System.out.println("\n** Belgian Cup Final **");
        this.belgianCupWinner = matchSimulator.simulateSingleMatch(finalists.get(0), finalists.get(1));
        System.out.println("\nBelgian Cup Winner: " + this.belgianCupWinner.getName());
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
                    phase1Fixtures.add(new Match(team1, team2));
                }
            }
        }
        Collections.shuffle(phase1Fixtures);
        System.out.println(this.phase1Fixtures.size() + " regular season matches scheduled.");
    }

    public void simulateRegularSeason() {
        for (Match match : this.phase1Fixtures) {
            matchSimulator.simulateMatch(match);
        }
    }

    public void performPlayoffSplit() {
        this.teams.sort(Comparator.comparingInt(Team::getPoints).reversed());

        championsPlayoffTeams = new ArrayList<>(this.teams.subList(0, 6));
        europePlayoffTeams = new ArrayList<>(this.teams.subList(6, 12));
        relegationPlayoffTeams = new ArrayList<>(this.teams.subList(12, 16));

        // Halve points (rounding up)
        for (Team team : championsPlayoffTeams) {
            team.points = (int) Math.ceil(team.points / 2.0);
        }
        for (Team team : europePlayoffTeams) {
            team.points = (int) Math.ceil(team.points / 2.0);
        }

        addPlayoffFixtures(championsPlayoffTeams, this.championsPlayoffFixtures);
        addPlayoffFixtures(europePlayoffTeams, this.europePlayoffFixtures);
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
        System.out.println("\n-- Simulating Champions' Play-off --");
        for (Match match : this.championsPlayoffFixtures) {
            matchSimulator.simulateMatch(match);
        }
        System.out.println("\n-- Simulating Europe Play-off --");
        for (Match match : this.europePlayoffFixtures) {
            matchSimulator.simulateMatch(match);
        }
    }

    public void determineEuropeanSpots() {
        Comparator<Team> tableSorter = getTableSorter();
        championsPlayoffTeams.sort(tableSorter);
        europePlayoffTeams.sort(tableSorter);

        Set<Team> qualifiedForEurope = new HashSet<>();

        // 1. Champions League
        uclTeams.add(championsPlayoffTeams.get(0)); // [UCL] Champion
        uclTeams.add(championsPlayoffTeams.get(1)); // [UCL-Q] Runner-up
        qualifiedForEurope.addAll(uclTeams);

        // 2. Europa League
        if (!qualifiedForEurope.contains(belgianCupWinner)) {
            uelTeams.add(belgianCupWinner);
            qualifiedForEurope.add(belgianCupWinner);
        }

        // If cup winner is already in UCL, UEL spot goes to 3rd place
        if (uelTeams.isEmpty()) {
            Team thirdPlace = championsPlayoffTeams.get(2);
            if (!qualifiedForEurope.contains(thirdPlace)) {
                uelTeams.add(thirdPlace);
                qualifiedForEurope.add(thirdPlace);
            }
        }

        // 3. Conference League
        // Winner of the Europe Play-off gets the spot
        Team europePlayoffWinner = europePlayoffTeams.get(0);
        if(!qualifiedForEurope.contains(europePlayoffWinner)){
            ueclTeams.add(europePlayoffWinner);
        }
    }

    private Comparator<Team> getTableSorter() {
        return (t1, t2) -> {
            if (t1.getPoints() != t2.getPoints()) {
                return Integer.compare(t2.getPoints(), t1.getPoints());
            }
            if (t1.getWins() != t2.getWins()) {
                return Integer.compare(t2.getWins(), t1.getWins());
            }
            if (t1.getGoalDifference() != t2.getGoalDifference()) {
                return Integer.compare(t2.getGoalDifference(), t1.getGoalDifference());
            }
            if (t1.getGoalsFor() != t2.getGoalsFor()) {
                return Integer.compare(t2.getGoalsFor(), t1.getGoalsFor());
            }
            return t1.getName().compareTo(t2.getName());
        };
    }

    public void displayTable(String title, boolean isFinal) {
        System.out.println("\n--- " + title + " ---");

        Comparator<Team> tableSorter = getTableSorter();

        if (isFinal) {
            championsPlayoffTeams.sort(tableSorter);
            europePlayoffTeams.sort(tableSorter);
            relegationPlayoffTeams.sort(tableSorter);

            System.out.println("\n** CHAMPIONS' PLAY-OFF **");
            printTableSection(championsPlayoffTeams, 1, true);

            System.out.println("\n** EUROPE PLAY-OFF **");
            printTableSection(europePlayoffTeams, 7, true);

            System.out.println("\n** RELEGATION GROUP **");
            printTableSection(relegationPlayoffTeams, 13, true);

        } else {
            List<Team> regularSeasonOrder = new ArrayList<>(this.teams);
            regularSeasonOrder.sort(tableSorter);
            printTableSection(regularSeasonOrder, 1, false);
        }
    }

    private void printTableSection(List<Team> sectionTeams, int startPosition, boolean isFinal) {
        System.out.println("Pos | Team                     | P  | W  | D  | L  | GF | GA | GD  | Pts | Elo ");
        System.out.println("------------------------------------------------------------------------------------");

        int position = startPosition;
        for (Team team : sectionTeams) {
            String teamDisplayName = team.getName();
            String qualificationMarker = "";

            if (isFinal) {
                if (uclTeams.contains(team)) {
                    qualificationMarker = team.equals(championsPlayoffTeams.get(0)) ? " [C][UCL]" : " [UCL-Q]";
                } else if (uelTeams.contains(team)) {
                    qualificationMarker = " [UEL]";
                } else if (ueclTeams.contains(team)) {
                    qualificationMarker = " [UECL-Q]";
                }

                if (position == 14) qualificationMarker += " [RPO]";
                else if (position >= 15) qualificationMarker += " [R]";
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

        if (isFinal && startPosition == 13) {
            System.out.println("------------------------------------------------------------------------------------");
            System.out.println("Legend: [C] Champions, [UCL] Champions League, [UCL-Q] UCL Qualifiers, [UEL] Europa League, [UECL-Q] UECL Qualifiers");
            System.out.println("        [RPO] Relegation Play-off, [R] Relegation");
            System.out.println("Cup Winner: [Belgian Cup: " + this.belgianCupWinner.getName() + "]");
        }
    }
}