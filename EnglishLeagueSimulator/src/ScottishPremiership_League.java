import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The main class for the Scottish Premiership Simulator.
 * This version simulates the 12-team league, the unique post-season split,
 * the Scottish Cup, and features accurate European/relegation rules.
 *
 * NOTE: This class has been refactored to work with the EuropeanCompetitionSimulator.
 */
public class ScottishPremiership_League {
    private final List<Team> teams;
    private final List<Match> phase1Fixtures;
    private final List<Match> phase2Fixtures;
    private final MatchSimulator matchSimulator;
    private final Random random = new Random();

    private Team scottishCupWinner;

    // Lists to store qualified teams
    private final List<Team> uclTeams = new ArrayList<>();
    private final List<Team> uelTeams = new ArrayList<>();
    private final List<Team> ueclTeams = new ArrayList<>();


    public ScottishPremiership_League() {
        this.teams = new ArrayList<>();
        this.phase1Fixtures = new ArrayList<>();
        this.phase2Fixtures = new ArrayList<>();
        this.matchSimulator = new MatchSimulator();
    }

    // --- GETTERS FOR EUROPEAN QUALIFIERS ---
    public List<Team> getUclTeams() { return uclTeams; }
    public List<Team> getUelTeams() { return uelTeams; }
    public List<Team> getUeclTeams() { return ueclTeams; }

    public static void main(String[] args) {
        ScottishPremiership_League scotland = new ScottishPremiership_League();

        System.out.println("--- Setting up the Scottish Premiership with real teams ---");
        scotland.setupTeams();

        System.out.println("\n--- Simulating the Scottish Cup... ---");
        scotland.simulateScottishCup();

        System.out.println("\n--- Generating League Fixtures (Phase 1)... ---");
        scotland.generatePhase1Fixtures();

        System.out.println("\n--- Simulating League Season (Phase 1)... ---");
        scotland.simulatePhase1();

        System.out.println("\n--- PRE-SPLIT TABLE ---");
        scotland.displayTable("PRE-SPLIT TABLE", false);

        System.out.println("\n--- Performing League Split and Generating Phase 2 Fixtures... ---");
        scotland.generatePhase2Fixtures();

        System.out.println("\n--- Simulating League Season (Phase 2)... ---");
        scotland.simulatePhase2();

        System.out.println("\n--- Determining European Spots ---");
        scotland.determineEuropeanSpots();

        System.out.println("\n--- FINAL SCOTTISH PREMIERSHIP TABLE ---");
        scotland.displayTable("FINAL SCOTTISH PREMIERSHIP TABLE", true);
    }

    public void setupTeams() {
        double initialElo = 1500;
        this.teams.add(new Team("Celtic", 98, 89, initialElo + 330));
        this.teams.add(new Team("Rangers", 94, 86, initialElo + 290));
        this.teams.add(new Team("Heart of Midlothian", 82, 80, initialElo + 150));
        this.teams.add(new Team("Hibernian", 80, 78, initialElo + 120));
        this.teams.add(new Team("Aberdeen", 79, 79, initialElo + 100));
        this.teams.add(new Team("St Mirren", 74, 76, initialElo + 20));
        this.teams.add(new Team("Dundee", 75, 72, initialElo));
        this.teams.add(new Team("Kilmarnock", 72, 75, initialElo - 20));
        this.teams.add(new Team("Motherwell", 71, 70, initialElo - 80));
        this.teams.add(new Team("Ross County", 68, 71, initialElo - 150));
        this.teams.add(new Team("St Johnstone", 66, 73, initialElo - 180));
        this.teams.add(new Team("Dundee United", 65, 68, initialElo - 220));
        System.out.println("12 Scottish Premiership teams have been created.");
    }

    public void simulateScottishCup() {
        List<Team> cupTeams = new ArrayList<>(this.teams);
        cupTeams.sort(Comparator.comparingDouble(Team::getEloRating).reversed());
        List<Team> preliminaryTeams = new ArrayList<>(cupTeams.subList(4, 12));
        List<Team> teamsWithByes = new ArrayList<>(cupTeams.subList(0, 4));

        System.out.println("\n** Scottish Cup Preliminary Round **");
        List<Team> round2Winners = new ArrayList<>();
        for (int i = 0; i < preliminaryTeams.size(); i += 2) {
            round2Winners.add(matchSimulator.simulateSingleMatch(preliminaryTeams.get(i), preliminaryTeams.get(i + 1)));
        }

        List<Team> quarterFinalists = new ArrayList<>(teamsWithByes);
        quarterFinalists.addAll(round2Winners);

        System.out.println("\n** Scottish Cup Quarter-Finals **");
        List<Team> semiFinalists = simulateKnockoutRound(quarterFinalists);
        System.out.println("\n** Scottish Cup Semi-Finals **");
        List<Team> finalists = simulateKnockoutRound(semiFinalists);
        System.out.println("\n** Scottish Cup Final **");
        this.scottishCupWinner = matchSimulator.simulateSingleMatch(finalists.get(0), finalists.get(1));
        System.out.println("\nScottish Cup Winner: " + this.scottishCupWinner.getName());
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

        // 1. UCL
        uclTeams.add(this.teams.get(0)); // Champion
        uclTeams.add(this.teams.get(1)); // Qualifier
        qualifiedForEurope.addAll(uclTeams);

        // 2. UEL Qualifier
        if (!qualifiedForEurope.contains(scottishCupWinner)) {
            uelTeams.add(scottishCupWinner);
            qualifiedForEurope.add(scottishCupWinner);
        }

        // 3. Fill remaining UECL spots
        int leagueSpotCounter = 2; // Start from 3rd place

        // If cup winner spot is not used, it passes down to 3rd place as a UEL spot
        if (uelTeams.isEmpty()) {
            while(uelTeams.isEmpty() && leagueSpotCounter < this.teams.size()){
                Team team = this.teams.get(leagueSpotCounter);
                if(!qualifiedForEurope.contains(team)){
                    uelTeams.add(team);
                    qualifiedForEurope.add(team);
                }
                leagueSpotCounter++;
            }
        }

        // Next two available spots get UECL
        while(ueclTeams.size() < 2 && leagueSpotCounter < this.teams.size()){
            Team team = this.teams.get(leagueSpotCounter);
            if(!qualifiedForEurope.contains(team)){
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
                    qualificationMarker = position == 1 ? " [C][UCL]" : " [UCL-Q]";
                } else if (uelTeams.contains(team)) {
                    qualificationMarker = " [UEL-Q]";
                } else if (ueclTeams.contains(team)) {
                    qualificationMarker = " [UECL-Q]";
                }

                if (position == 11) qualificationMarker += " [RPO]";
                else if (position == 12) qualificationMarker += " [R]";
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
            System.out.println("Legend: [C] Champion, [UCL] Champions League, [UCL-Q] UCL Qualifiers, [UEL-Q] UEL Qualifiers, [UECL-Q] UECL Qualifiers");
            System.out.println("        [RPO] Relegation Play-off, [R] Relegation");
            System.out.println("Cup Winner: [Scottish Cup: " + this.scottishCupWinner.getName() + "]");
        }
    }
}