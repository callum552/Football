import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The main class for the Italian Serie A Simulator.
 * This version simulates the 20-team league, a basic Coppa Italia, and features
 * the crucial title/relegation play-off tiebreaker rule.
 *
 * NOTE: This class has been refactored to work with the EuropeanCompetitionSimulator.
 */
public class SerieA_League {
    private final List<Team> teams;
    private final List<Match> fixtures;
    private final MatchSimulator matchSimulator;
    private final Random random = new Random();

    private Team coppaItaliaWinner;
    private Team titlePlayoffWinner = null;
    private Team relegationPlayoffLoser = null;

    // Lists to store qualified teams
    private final List<Team> uclTeams = new ArrayList<>();
    private final List<Team> uelTeams = new ArrayList<>();
    private final List<Team> ueclTeams = new ArrayList<>();


    public SerieA_League() {
        this.teams = new ArrayList<>();
        this.fixtures = new ArrayList<>();
        this.matchSimulator = new MatchSimulator();
    }

    // --- GETTERS FOR EUROPEAN QUALIFIERS ---
    public List<Team> getUclTeams() { return uclTeams; }
    public List<Team> getUelTeams() { return uelTeams; }
    public List<Team> getUeclTeams() { return ueclTeams; }


    public static void main(String[] args) {
        SerieA_League serieA = new SerieA_League();

        System.out.println("--- Setting up Serie A with real teams ---");
        serieA.setupTeams();

        System.out.println("\n--- Simulating the Coppa Italia... ---");
        serieA.simulateCoppaItalia();

        System.out.println("\n--- Generating League Fixtures ---");
        serieA.generateFixtures();

        System.out.println("\n--- Simulating League Season with Dominance Model... ---");
        serieA.simulateSeason();

        System.out.println("\n--- Checking for End-of-Season Play-offs... ---");
        serieA.checkForAndSimulatePlayoffs();

        System.out.println("\n--- Determining European Spots ---");
        serieA.determineEuropeanSpots();

        System.out.println("\n--- FINAL SERIE A TABLE ---");
        serieA.displayTable();
    }

    public void setupTeams() {
        double initialElo = 1500;
        this.teams.add(new Team("Inter Milan", 92, 89, initialElo + 210));
        this.teams.add(new Team("AC Milan", 89, 85, initialElo + 190));
        this.teams.add(new Team("Juventus", 87, 88, initialElo + 180));
        this.teams.add(new Team("Napoli", 88, 82, initialElo + 160));
        this.teams.add(new Team("AS Roma", 85, 84, initialElo + 150));
        this.teams.add(new Team("Atalanta", 86, 80, initialElo + 140));
        this.teams.add(new Team("Lazio", 84, 83, initialElo + 130));
        this.teams.add(new Team("Fiorentina", 82, 79, initialElo + 80));
        this.teams.add(new Team("Bologna", 80, 81, initialElo + 70));
        this.teams.add(new Team("Torino", 77, 82, initialElo + 50));
        this.teams.add(new Team("Monza", 78, 77, initialElo));
        this.teams.add(new Team("Genoa", 75, 78, initialElo - 20));
        this.teams.add(new Team("Udinese", 76, 76, initialElo - 40));
        this.teams.add(new Team("Sassuolo", 79, 72, initialElo - 60));
        this.teams.add(new Team("Lecce", 72, 75, initialElo - 100));
        this.teams.add(new Team("Empoli", 70, 77, initialElo - 120));
        this.teams.add(new Team("Hellas Verona", 71, 74, initialElo - 140));
        this.teams.add(new Team("Salernitana", 73, 70, initialElo - 160));
        this.teams.add(new Team("Cagliari", 68, 71, initialElo - 200));
        this.teams.add(new Team("Frosinone", 67, 69, initialElo - 220));
        System.out.println("20 Serie A teams have been created.");
    }

    public void simulateCoppaItalia() {
        List<Team> cupTeams = new ArrayList<>(this.teams);
        System.out.println("\n** Coppa Italia Preliminary Round **");
        List<Team> roundOf16Teams = getRoundOf16Teams(cupTeams);
        System.out.println("\n** Coppa Italia Round of 16 **");
        List<Team> quarterFinalists = simulateKnockoutRound(roundOf16Teams);
        System.out.println("\n** Coppa Italia Quarter-Finals **");
        List<Team> semiFinalists = simulateKnockoutRound(quarterFinalists);
        System.out.println("\n** Coppa Italia Semi-Finals **");
        List<Team> finalists = simulateKnockoutRound(semiFinalists);
        System.out.println("\n** Coppa Italia Final **");
        this.coppaItaliaWinner = matchSimulator.simulateSingleMatch(finalists.get(0), finalists.get(1));
        System.out.println("\nCoppa Italia Winner: " + this.coppaItaliaWinner.getName());
    }

    private List<Team> getRoundOf16Teams(List<Team> cupTeams) {
        Collections.shuffle(cupTeams, random);
        cupTeams.sort(Comparator.comparingDouble(Team::getEloRating).reversed());
        List<Team> preliminaryRoundTeams = new ArrayList<>(cupTeams.subList(12, 20));
        List<Team> teamsWithByes = new ArrayList<>(cupTeams.subList(0, 12));
        List<Team> preliminaryWinners = new ArrayList<>();
        for (int i = 0; i < preliminaryRoundTeams.size(); i += 2) {
            preliminaryWinners.add(matchSimulator.simulateSingleMatch(preliminaryRoundTeams.get(i), preliminaryRoundTeams.get(i+1)));
        }
        List<Team> roundOf16Teams = new ArrayList<>(teamsWithByes);
        roundOf16Teams.addAll(preliminaryWinners);
        Collections.shuffle(roundOf16Teams, random);
        return roundOf16Teams;
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

    public void simulateSeason() {
        for (Match match : this.fixtures) {
            matchSimulator.simulateMatch(match);
        }
    }

    public void checkForAndSimulatePlayoffs() {
        this.teams.sort(Comparator.comparingInt(Team::getPoints).reversed()
                .thenComparingInt(Team::getGoalDifference).reversed());

        Team first = this.teams.get(0);
        Team second = this.teams.get(1);
        if (first.getPoints() == second.getPoints()) {
            System.out.println("\n** SERIE A TITLE PLAY-OFF **");
            titlePlayoffWinner = matchSimulator.simulateSingleMatch(first, second);
            System.out.println(titlePlayoffWinner.getName() + " are crowned champions!");
        }

        Team seventeenth = this.teams.get(16);
        Team eighteenth = this.teams.get(17);
        if (seventeenth.getPoints() == eighteenth.getPoints()) {
            System.out.println("\n** SERIE A RELEGATION PLAY-OFF **");
            Team winner = matchSimulator.simulateSingleMatch(seventeenth, eighteenth);
            relegationPlayoffLoser = winner.equals(seventeenth) ? eighteenth : seventeenth;
            System.out.println(relegationPlayoffLoser.getName() + " are relegated.");
        }
    }

    public void determineEuropeanSpots() {
        // Sort table using the standard tie-breakers first to determine initial placings
        this.teams.sort(Comparator.comparingInt(Team::getPoints).reversed()
                .thenComparingInt(Team::getGoalDifference).reversed()
                .thenComparingInt(Team::getGoalsFor).reversed());

        Set<Team> qualifiedForEurope = new HashSet<>();

        // 1. UCL spots (Top 4)
        for (int i = 0; i < 4; i++) {
            uclTeams.add(this.teams.get(i));
        }
        qualifiedForEurope.addAll(uclTeams);

        // 2. Coppa Italia winner gets a UEL spot
        if (!qualifiedForEurope.contains(coppaItaliaWinner)) {
            uelTeams.add(coppaItaliaWinner);
            qualifiedForEurope.add(coppaItaliaWinner);
        }

        // 3. Fill remaining UEL/UECL spots from league table
        int leagueSpotCounter = 4;
        while (uelTeams.size() < 2 && leagueSpotCounter < this.teams.size()) {
            Team team = this.teams.get(leagueSpotCounter);
            if (!qualifiedForEurope.contains(team)) {
                uelTeams.add(team);
                qualifiedForEurope.add(team);
            }
            leagueSpotCounter++;
        }
        while (ueclTeams.isEmpty() && leagueSpotCounter < this.teams.size()) {
            Team team = this.teams.get(leagueSpotCounter);
            if (!qualifiedForEurope.contains(team)) {
                ueclTeams.add(team);
                qualifiedForEurope.add(team);
            }
            leagueSpotCounter++;
        }
    }


    public void displayTable() {
        // Final sort of the table, accounting for playoffs
        this.teams.sort((t1, t2) -> {
            if (t1.getPoints() != t2.getPoints()) return Integer.compare(t2.getPoints(), t1.getPoints());
            // If a title play-off happened, the winner is first
            if (titlePlayoffWinner != null && (t1.equals(titlePlayoffWinner) || t2.equals(titlePlayoffWinner))) {
                return t1.equals(titlePlayoffWinner) ? -1 : 1;
            }
            // If a relegation play-off happened, the loser is lower
            if (relegationPlayoffLoser != null && (t1.equals(relegationPlayoffLoser) || t2.equals(relegationPlayoffLoser))) {
                return t1.equals(relegationPlayoffLoser) ? 1 : -1;
            }
            if (t1.getGoalDifference() != t2.getGoalDifference()) return Integer.compare(t2.getGoalDifference(), t1.getGoalDifference());
            if (t1.getGoalsFor() != t2.getGoalsFor()) return Integer.compare(t2.getGoalsFor(), t1.getGoalsFor());
            return t1.getName().compareTo(t2.getName());
        });


        System.out.println("Pos | Team                     | P  | W  | D  | L  | GF | GA | GD  | Pts | Elo ");
        System.out.println("------------------------------------------------------------------------------------");

        int position = 1;
        for (Team team : this.teams) {
            String teamDisplayName = team.getName();
            String qualificationMarker = "";

            if (team.equals(titlePlayoffWinner) || (position == 1 && titlePlayoffWinner == null)) {
                qualificationMarker = " [C][UCL]";
            } else if (uclTeams.contains(team)) {
                qualificationMarker = " [UCL]";
            } else if (uelTeams.contains(team)) {
                qualificationMarker = " [UEL]";
            } else if (ueclTeams.contains(team)) {
                qualificationMarker = " [UECL]";
            }

            // Relegation logic now accounts for the playoff loser
            if (team.equals(relegationPlayoffLoser)) {
                qualificationMarker += " [R]";
            } else if (position >= 18 && (this.teams.indexOf(team) > 16) ) {
                // A bit more robust check for teams in the relegation zone that weren't the playoff winner
                if (relegationPlayoffLoser == null || !team.equals(this.teams.stream().filter(t -> t.getPoints() == relegationPlayoffLoser.getPoints()).findFirst().get() ) ) {
                    qualificationMarker += " [R]";
                }
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
        System.out.println("Legend: [C] Champions, [UCL] Champions League, [UEL] Europa League, [UECL] Europa Conference League, [R] Relegation");
        System.out.println("Cup Winner: [Coppa Italia: " + this.coppaItaliaWinner.getName() + "]");
    }
}