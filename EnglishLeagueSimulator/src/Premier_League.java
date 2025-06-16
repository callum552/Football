import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * The main class for the English Premier League Simulator.
 * This version simulates the 20-team league, a scaled back FA and League Cup with those teams,
 * and features the correct, multi-stage tie-breaker rules.
 *
 * NOTE: This class has been refactored to work with the EuropeanCompetitionSimulator.
 */
public class Premier_League {
    private final List<Team> teams;
    private final List<Match> fixtures;
    private final MatchSimulator matchSimulator;
    private final Random random = new Random();

    private Team faCupWinner;
    private Team leagueCupWinner;

    // Lists to store qualified teams
    private List<Team> uclTeams = new ArrayList<>();
    private List<Team> uelTeams = new ArrayList<>();
    private List<Team> ueclTeams = new ArrayList<>();


    public Premier_League() {
        this.teams = new ArrayList<>();
        this.fixtures = new ArrayList<>();
        this.matchSimulator = new MatchSimulator();
    }

    // --- GETTERS FOR QUALIFIED TEAMS ---
    public List<Team> getUclTeams() { return uclTeams; }
    public List<Team> getUelTeams() { return uelTeams; }
    public List<Team> getUeclTeams() { return ueclTeams; }


    public static void main(String[] args) {
        Premier_League premierLeague = new Premier_League();

        System.out.println("--- Setting up the Premier League with real teams ---");
        premierLeague.setupTeams();

        premierLeague.simulateLeagueCup();
        premierLeague.simulateFACup();

        System.out.println("\n--- Generating League Fixtures ---");
        premierLeague.generateFixtures();

        System.out.println("\n--- Simulating League Season with Dominance Model... ---");
        premierLeague.simulateSeason();

        System.out.println("\n--- Determining European Spots ---");
        premierLeague.determineEuropeanSpots();

        System.out.println("\n--- FINAL PREMIER LEAGUE TABLE ---");
        premierLeague.displayTable();
    }

    public void setupTeams() {
        double initialElo = 1500;
        this.teams.add(new Team("Manchester City", 92, 88, initialElo + 200));
        this.teams.add(new Team("Arsenal", 90, 86, initialElo + 180));
        this.teams.add(new Team("Liverpool", 88, 89, initialElo + 175));
        this.teams.add(new Team("Manchester United", 87, 82, initialElo + 160));
        this.teams.add(new Team("Tottenham Hotspur", 89, 83, initialElo + 150));
        this.teams.add(new Team("Chelsea", 85, 84, initialElo + 140));
        this.teams.add(new Team("Newcastle United", 84, 80, initialElo + 100));
        this.teams.add(new Team("Aston Villa", 83, 79, initialElo + 90));
        this.teams.add(new Team("Brighton & Hove Albion", 80, 81, initialElo + 50));
        this.teams.add(new Team("West Ham United", 79, 78, initialElo));
        this.teams.add(new Team("Wolverhampton Wanderers", 77, 76, initialElo - 10));
        this.teams.add(new Team("Crystal Palace", 78, 75, initialElo - 20));
        this.teams.add(new Team("Brentford", 75, 77, initialElo - 30));
        this.teams.add(new Team("Fulham", 74, 72, initialElo - 80));
        this.teams.add(new Team("Everton", 72, 74, initialElo - 85));
        this.teams.add(new Team("Nottingham Forest", 70, 73, initialElo - 100));
        this.teams.add(new Team("Bournemouth", 66, 68, initialElo - 200));
        this.teams.add(new Team("Burnley", 64, 67, initialElo - 220));
        this.teams.add(new Team("Luton Town", 62, 65, initialElo - 240));
        this.teams.add(new Team("Sheffield United", 61, 64, initialElo - 250));
        System.out.println("20 Premier League teams have been created.");
    }

    public void simulateSeason() {
        for (Match match : this.fixtures) {
            matchSimulator.simulateMatch(match);
        }
    }

    // --- CORRECTED CUP SIMULATION METHODS ---

    public void simulateLeagueCup() {
        System.out.println("\n--- Simulating the League Cup... ---");
        List<Team> cupTeams = new ArrayList<>(this.teams);
        this.leagueCupWinner = runFullKnockoutTournament(cupTeams, "League Cup");
        System.out.println("\nLeague Cup Winner: " + this.leagueCupWinner.getName());
    }

    public void simulateFACup() {
        System.out.println("\n--- Simulating the FA Cup... ---");
        List<Team> cupTeams = new ArrayList<>(this.teams);
        this.faCupWinner = runFullKnockoutTournament(cupTeams, "FA Cup");
        System.out.println("\nFA Cup Winner: " + this.faCupWinner.getName());
    }

    /**
     * NEW METHOD: Simulates an entire knockout cup from start to finish.
     * @param teams The list of teams participating in the cup.
     * @param cupName The name of the cup for display purposes.
     * @return The winning Team.
     */
    private Team runFullKnockoutTournament(List<Team> teams, String cupName) {
        List<Team> currentRoundTeams = new ArrayList<>(teams);

        // Simplified: For 20 teams, we'll have a preliminary round for 8 teams to get down to 16
        if(currentRoundTeams.size() > 16) {
            System.out.println("\n** " + cupName + " Preliminary Round **");
            List<Team> preliminaryWinners = new ArrayList<>();
            // Sort to ensure lower-ranked teams play this round
            currentRoundTeams.sort(Comparator.comparingDouble(Team::getEloRating));
            List<Team> teamsInRound = currentRoundTeams.subList(0, 8);
            List<Team> teamsWithByes = currentRoundTeams.subList(8, 20);

            for(int i = 0; i < teamsInRound.size(); i += 2) {
                preliminaryWinners.add(matchSimulator.simulateSingleMatch(teamsInRound.get(i), teamsInRound.get(i + 1)));
            }
            currentRoundTeams = new ArrayList<>(teamsWithByes);
            currentRoundTeams.addAll(preliminaryWinners);
        }

        while(currentRoundTeams.size() > 1) {
            String roundName;
            if (currentRoundTeams.size() <= 2) roundName = "Final";
            else if (currentRoundTeams.size() <= 4) roundName = "Semi-Finals";
            else if (currentRoundTeams.size() <= 8) roundName = "Quarter-Finals";
            else if (currentRoundTeams.size() <= 16) roundName = "Round of 16";
            else roundName = "Early Round";

            System.out.println("\n** " + cupName + " " + roundName + " **");
            currentRoundTeams = simulateKnockoutRound(currentRoundTeams);
        }
        return currentRoundTeams.get(0);
    }

    private List<Team> simulateKnockoutRound(List<Team> teamsInRound) {
        List<Team> winners = new ArrayList<>();
        Collections.shuffle(teamsInRound, random);
        for (int i = 0; i < teamsInRound.size(); i += 2) {
            winners.add(matchSimulator.simulateSingleMatch(teamsInRound.get(i), teamsInRound.get(i + 1)));
        }
        return winners;
    }


    public void generateFixtures() {
        for (Team homeTeam : this.teams) {
            for (Team awayTeam : this.teams) {
                if (!homeTeam.equals(awayTeam)) {
                    this.fixtures.add(new Match(homeTeam, awayTeam));
                }
            }
        }
        System.out.println(this.fixtures.size() + " league matches have been scheduled.");
    }


    public void determineEuropeanSpots() {
        this.teams.sort(Comparator.comparingInt(Team::getPoints).reversed()
                .thenComparingInt(Team::getGoalDifference).reversed()
                .thenComparingInt(Team::getGoalsFor).reversed());

        Set<Team> qualifiedForEurope = new HashSet<>();

        // 1. UCL spots (Top 5)
        for (int i = 0; i < 5 && i < this.teams.size(); i++) {
            uclTeams.add(this.teams.get(i));
        }
        qualifiedForEurope.addAll(uclTeams);

        // 2. FA Cup winner spot
        if (!qualifiedForEurope.contains(faCupWinner)) {
            uelTeams.add(faCupWinner);
            qualifiedForEurope.add(faCupWinner);
        }

        // 3. Fill remaining UEL spot from league
        int leagueSpotCounter = 5;
        while (uelTeams.size() < 2 && leagueSpotCounter < this.teams.size()) {
            Team team = this.teams.get(leagueSpotCounter);
            if (!qualifiedForEurope.contains(team)) {
                uelTeams.add(team);
                qualifiedForEurope.add(team);
            }
            leagueSpotCounter++;
        }

        // 4. League Cup winner spot (or pass down)
        Team ueclTeam = null;
        if (!qualifiedForEurope.contains(leagueCupWinner)) {
            ueclTeam = leagueCupWinner;
        } else {
            while (ueclTeam == null && leagueSpotCounter < this.teams.size()) {
                Team team = this.teams.get(leagueSpotCounter);
                if (!qualifiedForEurope.contains(team)) {
                    ueclTeam = team;
                    qualifiedForEurope.add(team);
                }
                leagueSpotCounter++;
            }
        }
        if (ueclTeam != null) {
            ueclTeams.add(ueclTeam);
        }
    }


    public void displayTable() {
        System.out.println("Pos | Team                     | P  | W  | D  | L  | GF | GA | GD  | Pts | Elo ");
        System.out.println("------------------------------------------------------------------------------------");

        int position = 1;
        for (Team team : this.teams) {
            String teamDisplayName = team.getName();
            String qualificationMarker = "";

            if (uclTeams.contains(team)) {
                qualificationMarker = (position == 1) ? " [C][UCL]" : " [UCL]";
            } else if (uelTeams.contains(team)) {
                qualificationMarker = " [UEL]";
            } else if (ueclTeams.contains(team)) {
                qualificationMarker = " [UECL]";
            }

            if (position >= 18) {
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
                    team.points,
                    team.getEloRating());
        }
        System.out.println("------------------------------------------------------------------------------------");
        System.out.println("Legend: [C] Champions, [UCL] Champions League, [UEL] Europa League, [UECL] Europa Conference League, [R] Relegation");
        System.out.println("Cup Winners: [FA Cup: " + this.faCupWinner.getName() + "] [League Cup: " + this.leagueCupWinner.getName() + "]");
    }
}