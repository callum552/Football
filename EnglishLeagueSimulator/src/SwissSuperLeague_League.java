import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * The main class for the Swiss Super League Simulator.
 * This version simulates the 12-team league, the three-phase season with a
 * post-season split, the Swiss Cup, and features accurate European/relegation
 * rules.
 *
 * NOTE: This class has been refactored to work with the
 * EuropeanCompetitionSimulator.
 */
public class SwissSuperLeague_League {
    private final List<Team> teams;
    private final List<Match> phase1Fixtures;
    private final List<Match> phase2Fixtures;
    private final MatchSimulator matchSimulator;
    private final Random random = new Random();

    private Team swissCupWinner;

    // Lists to store qualified teams
    private final List<Team> uclTeams = new ArrayList<>();
    private final List<Team> uelTeams = new ArrayList<>();
    private final List<Team> ueclTeams = new ArrayList<>();

    public SwissSuperLeague_League() {
        this.teams = new ArrayList<>();
        this.phase1Fixtures = new ArrayList<>();
        this.phase2Fixtures = new ArrayList<>();
        this.matchSimulator = new MatchSimulator();
    }

    // --- GETTERS FOR EUROPEAN QUALIFIERS ---
    public List<Team> getUclTeams() {
        return uclTeams;
    }

    public List<Team> getUelTeams() {
        return uelTeams;
    }

    public List<Team> getUeclTeams() {
        return ueclTeams;
    }

    public static void main(String[] args) {
        SwissSuperLeague_League switzerland = new SwissSuperLeague_League();

        System.out.println("--- Setting up the Swiss Super League with real teams ---");
        switzerland.setupTeams();

        System.out.println("\n--- Simulating the Swiss Cup... ---");
        switzerland.simulateSwissCup();

        System.out.println("\n--- Generating League Fixtures (Phase 1)... ---");
        switzerland.generatePhase1Fixtures();

        System.out.println("\n--- Simulating League Season (Phase 1)... ---");
        switzerland.simulatePhase1();

        System.out.println("\n--- PRE-SPLIT TABLE (33 MATCHES) ---");
        switzerland.displayTable("PRE-SPLIT TABLE", false);

        System.out.println("\n--- Performing League Split and Generating Phase 2 Fixtures... ---");
        switzerland.generatePhase2Fixtures();

        System.out.println("\n--- Simulating League Season (Phase 2)... ---");
        switzerland.simulatePhase2();

        System.out.println("\n--- Determining European Spots ---");
        switzerland.determineEuropeanSpots();

        System.out.println("\n--- FINAL SWISS SUPER LEAGUE TABLE ---");
        switzerland.displayTable("FINAL SWISS SUPER LEAGUE TABLE", true);
    }

    public void setupTeams() {
        double initialElo = 1500;
        this.teams.add(new Team("BSC Young Boys", 90, 84, initialElo + 80)); // Adjusted
        this.teams.add(new Team("FC Basel", 86, 82, initialElo + 60)); // Adjusted
        this.teams.add(new Team("FC Lugano", 84, 80, initialElo + 40)); // Adjusted
        this.teams.add(new Team("Servette FC", 82, 81, initialElo + 30)); // Adjusted
        this.teams.add(new Team("FC St. Gallen", 81, 78, initialElo + 20)); // Adjusted
        this.teams.add(new Team("FC Zürich", 83, 79, initialElo + 10)); // Adjusted
        this.teams.add(new Team("FC Luzern", 78, 77, initialElo - 10)); // Adjusted
        this.teams.add(new Team("Grasshopper Club Zürich", 76, 75, initialElo - 20)); // Adjusted
        this.teams.add(new Team("FC Winterthur", 72, 76, initialElo - 100)); // Adjusted
        this.teams.add(new Team("Yverdon-Sport FC", 70, 74, initialElo - 150)); // Adjusted
        this.teams.add(new Team("FC Lausanne-Sport", 68, 72, initialElo - 180)); // Adjusted
        this.teams.add(new Team("Stade Lausanne-Ouchy", 65, 70, initialElo - 220)); // Adjusted
        System.out.println("12 Swiss Super League teams have been created.");
    }

    public void simulateSwissCup() {
        List<Team> cupTeams = new ArrayList<>(this.teams);
        cupTeams.sort(Comparator.comparingDouble(Team::getEloRating).reversed());
        List<Team> preliminaryTeams = new ArrayList<>(cupTeams.subList(4, 12));
        List<Team> teamsWithByes = new ArrayList<>(cupTeams.subList(0, 4));

        System.out.println("\n** Swiss Cup Preliminary Round **");
        List<Team> round2Winners = new ArrayList<>();
        for (int i = 0; i < preliminaryTeams.size(); i += 2) {
            round2Winners.add(matchSimulator.simulateSingleMatch(preliminaryTeams.get(i), preliminaryTeams.get(i + 1)));
        }

        List<Team> quarterFinalists = new ArrayList<>(teamsWithByes);
        quarterFinalists.addAll(round2Winners);

        System.out.println("\n** Swiss Cup Quarter-Finals **");
        List<Team> semiFinalists = simulateKnockoutRound(quarterFinalists);
        System.out.println("\n** Swiss Cup Semi-Finals **");
        List<Team> finalists = simulateKnockoutRound(semiFinalists);
        System.out.println("\n** Swiss Cup Final **");
        this.swissCupWinner = matchSimulator.simulateSingleMatch(finalists.get(0), finalists.get(1));
        System.out.println("\nSwiss Cup Winner: " + this.swissCupWinner.getName());
    }

    private List<Team> simulateKnockoutRound(List<Team> teamsInRound) {
        List<Team> winners = new ArrayList<>();
        Collections.shuffle(teamsInRound, random);
        for (int i = 0; i < teamsInRound.size(); i += 2) {
            winners.add(matchSimulator.simulateSingleMatch(teamsInRound.get(i), teamsInRound.get(i + 1)));
        }
        return winners;
    }

    public void generatePhase1Fixtures() {
        this.phase1Fixtures.clear();
        for (Team team1 : this.teams) {
            for (Team team2 : this.teams) {
                if (!team1.equals(team2)) {
                    phase1Fixtures.add(new Match(team1, team2));
                }
            }
        }
        for (int i = 0; i < this.teams.size(); i++) {
            for (int j = i + 1; j < this.teams.size(); j++) {
                phase1Fixtures.add(new Match(this.teams.get(i), this.teams.get(j)));
            }
        }
        Collections.shuffle(phase1Fixtures);
        System.out.println(this.phase1Fixtures.size() + " Phase 1 matches scheduled.");
    }

    public void generatePhase2Fixtures() {
        this.phase2Fixtures.clear();
        this.teams.sort(Comparator.comparingInt(Team::getPoints).reversed());
        List<Team> topSix = new ArrayList<>(this.teams.subList(0, 6));
        List<Team> bottomSix = new ArrayList<>(this.teams.subList(6, 12));

        addSplitFixtures(topSix);
        addSplitFixtures(bottomSix);

        Collections.shuffle(phase2Fixtures);
        System.out.println("Post-split fixtures have been generated (" + this.phase2Fixtures.size() + " matches).");
    }

    private void addSplitFixtures(List<Team> section) {
        for (int i = 0; i < section.size(); i++) {
            for (int j = i + 1; j < section.size(); j++) {
                phase2Fixtures.add(new Match(section.get(i), section.get(j)));
            }
        }
    }

    public void simulatePhase1() {
        for (Match match : this.phase1Fixtures) {
            matchSimulator.simulateMatch(match);
        }
    }

    public void simulatePhase2() {
        for (Match match : this.phase2Fixtures) {
            matchSimulator.simulateMatch(match);
        }
    }

    public void determineEuropeanSpots() {
        Comparator<Team> tableSorter = getTableSorter();
        this.teams.sort(tableSorter);

        Set<Team> qualifiedForEurope = new HashSet<>();

        // 1. UCL Qualifiers
        uclTeams.add(this.teams.get(0)); // Champion
        uclTeams.add(this.teams.get(1)); // Runner-up
        qualifiedForEurope.addAll(uclTeams);

        // 2. UEL Play-off
        if (!qualifiedForEurope.contains(swissCupWinner)) {
            uelTeams.add(swissCupWinner);
            qualifiedForEurope.add(swissCupWinner);
        }

        // 3. Fill remaining UECL spots
        int leagueSpotCounter = 2; // Start from 3rd place

        // If cup winner spot is not used, it passes down to 3rd place as a UEL spot
        if (uelTeams.isEmpty()) {
            while (uelTeams.isEmpty() && leagueSpotCounter < this.teams.size()) {
                Team team = this.teams.get(leagueSpotCounter);
                if (!qualifiedForEurope.contains(team)) {
                    uelTeams.add(team);
                    qualifiedForEurope.add(team);
                }
                leagueSpotCounter++;
            }
        }

        // Next two available spots get UECL
        while (ueclTeams.size() < 2 && leagueSpotCounter < this.teams.size()) {
            Team team = this.teams.get(leagueSpotCounter);
            if (!qualifiedForEurope.contains(team)) {
                ueclTeams.add(team);
                qualifiedForEurope.add(team);
            }
            leagueSpotCounter++;
        }
    }

    private Comparator<Team> getTableSorter() {
        return Comparator.comparingInt(Team::getPoints).reversed()
                .thenComparingInt(Team::getGoalDifference).reversed()
                .thenComparingInt(Team::getGoalsFor).reversed()
                .thenComparing(Team::getName);
    }

    public void displayTable(String title, boolean isFinalTable) {
        System.out.println("\n--- " + title + " ---");

        Comparator<Team> tableSorter = getTableSorter();
        List<Team> finalOrder = new ArrayList<>();

        if (isFinalTable) {
            this.teams.sort(tableSorter);
            List<Team> topSix = new ArrayList<>(this.teams.subList(0, 6));
            List<Team> bottomSix = new ArrayList<>(this.teams.subList(6, 12));
            topSix.sort(tableSorter);
            bottomSix.sort(tableSorter);
            finalOrder.addAll(topSix);
            finalOrder.addAll(bottomSix);
        } else {
            finalOrder.addAll(this.teams);
            finalOrder.sort(tableSorter);
        }

        System.out.println("Pos | Team                     | P  | W  | D  | L  | GF | GA | GD  | Pts | Elo ");
        System.out.println("------------------------------------------------------------------------------------");

        int position = 1;
        for (Team team : finalOrder) {
            String teamDisplayName = team.getName();
            String qualificationMarker = "";

            if (isFinalTable) {
                if (uclTeams.contains(team)) {
                    qualificationMarker = (position == 1) ? " [C][UCL-Q]" : " [UCL-Q]";
                } else if (uelTeams.contains(team)) {
                    qualificationMarker = " [UEL-PO]";
                } else if (ueclTeams.contains(team)) {
                    qualificationMarker = " [UECL-Q]";
                }

                if (position == 11)
                    qualificationMarker += " [RPO]";
                else if (position == 12)
                    qualificationMarker += " [R]";
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

        System.out.println("------------------------------------------------------------------------------------");
        if (isFinalTable) {
            System.out.println(
                    "Legend: [C] Champion, [UCL-Q] Champions League Qualifiers, [UEL-PO] UEL Play-off, [UECL-Q] UECL Qualifiers");
            System.out.println("        [RPO] Relegation Play-off, [R] Relegation");
            System.out.println("Cup Winner: [Swiss Cup: " + this.swissCupWinner.getName() + "]");
        }
    }
}