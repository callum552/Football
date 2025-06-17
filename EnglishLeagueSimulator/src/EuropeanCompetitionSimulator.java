import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer; // Added for functional interface

/**
 * The main entry point for the entire European Football Simulation.
 * This class orchestrates the simulation of all major domestic leagues
 * and then uses their results to simulate the UEFA Champions League,
 * UEFA Europa League, and UEFA Europa Conference League.
 */
public class EuropeanCompetitionSimulator {

    public static void main(String[] args) {
        // --- 1. SIMULATE ALL DOMESTIC LEAGUES ---
        System.out.println("--- STARTING DOMESTIC LEAGUE SIMULATIONS ---");

        Premier_League premierLeague = new Premier_League();
        premierLeague.setupTeams();
        premierLeague.simulateLeagueCup();
        premierLeague.simulateFACup();
        premierLeague.generateFixtures();
        premierLeague.simulateSeason();
        premierLeague.determineEuropeanSpots();

        LaLiga_League laLiga = new LaLiga_League();
        laLiga.setupTeams();
        laLiga.simulateCopaDelRey();
        laLiga.generateFixtures();
        laLiga.simulateSeason();
        laLiga.determineEuropeanSpots();

        Bundesliga_League bundesliga = new Bundesliga_League();
        bundesliga.setupTeams();
        bundesliga.simulateDFBPokal();
        bundesliga.generateFixtures();
        bundesliga.simulateSeason();
        bundesliga.determineEuropeanSpots();

        SerieA_League serieA = new SerieA_League();
        serieA.setupTeams();
        serieA.simulateCoppaItalia();
        serieA.generateFixtures();
        serieA.simulateSeason();
        serieA.checkForAndSimulatePlayoffs(); // This needs to be called before determineEuropeanSpots for Serie A
        serieA.determineEuropeanSpots();

        Ligue1_League ligue1 = new Ligue1_League();
        ligue1.setupTeams();
        ligue1.simulateCoupeDeFrance();
        ligue1.generateFixtures();
        ligue1.simulateSeason();
        ligue1.determineEuropeanSpots();

        Eredivisie_League eredivisie = new Eredivisie_League();
        eredivisie.setupTeams();
        eredivisie.simulateKNVBBeker();
        eredivisie.generateFixtures();
        eredivisie.simulateSeason();
        eredivisie.simulateUECLPlayoffs(); // This needs to be called before determineEuropeanSpots for Eredivisie
        eredivisie.determineEuropeanSpots();

        LigaPortugal_League ligaPortugal = new LigaPortugal_League();
        ligaPortugal.setupTeams();
        ligaPortugal.simulateTacaDePortugal();
        ligaPortugal.generateFixtures();
        ligaPortugal.simulateSeason();
        ligaPortugal.determineEuropeanSpots();

        SwissSuperLeague_League swissSuperLeague = new SwissSuperLeague_League(); // Corrected class name
        swissSuperLeague.setupTeams();
        swissSuperLeague.simulateSwissCup();
        swissSuperLeague.generatePhase1Fixtures();
        swissSuperLeague.simulatePhase1();
        swissSuperLeague.generatePhase2Fixtures();
        swissSuperLeague.simulatePhase2();
        swissSuperLeague.determineEuropeanSpots();

        BelgianProLeague_League belgianProLeague = new BelgianProLeague_League();
        belgianProLeague.setupTeams();
        belgianProLeague.simulateBelgianCup();
        belgianProLeague.generateRegularSeasonFixtures();
        belgianProLeague.simulateRegularSeason();
        belgianProLeague.performPlayoffSplit();
        belgianProLeague.simulatePlayoffs();
        belgianProLeague.determineEuropeanSpots();

        AustrianBundesliga_League austrianBundesliga = new AustrianBundesliga_League();
        austrianBundesliga.setupTeams();
        austrianBundesliga.simulateOFBCup();
        austrianBundesliga.generateRegularSeasonFixtures();
        austrianBundesliga.simulateRegularSeason();
        austrianBundesliga.performPlayoffSplit();
        austrianBundesliga.simulatePlayoffs();
        austrianBundesliga.determineEuropeanSpots();

        ScottishPremiership_League scottishPremiership = new ScottishPremiership_League();
        scottishPremiership.setupTeams();
        scottishPremiership.simulateScottishCup();
        scottishPremiership.generatePhase1Fixtures();
        scottishPremiership.simulatePhase1();
        scottishPremiership.generatePhase2Fixtures();
        scottishPremiership.simulatePhase2();
        scottishPremiership.determineEuropeanSpots();

        TurkishSuperLig_League turkishSuperLig = new TurkishSuperLig_League();
        turkishSuperLig.setupTeams();
        turkishSuperLig.simulateTurkishCup();
        turkishSuperLig.generateFixtures();
        turkishSuperLig.simulateSeason();
        turkishSuperLig.determineEuropeanSpots();

        // --- 2. DISTRIBUTE TEAMS TO EUROPEAN COMPETITIONS BASED ON MERIT (CASCADING
        // QUALIFICATION) ---
        List<Team> finalUclTeams = new ArrayList<>();
        List<Team> finalUelTeams = new ArrayList<>();
        List<Team> finalUeclTeams = new ArrayList<>();

        distributeEuropeanTeams(
                premierLeague, laLiga, bundesliga, serieA, ligue1, eredivisie,
                ligaPortugal, swissSuperLeague, belgianProLeague, austrianBundesliga,
                scottishPremiership, turkishSuperLig,
                finalUclTeams, finalUelTeams, finalUeclTeams);

        System.out.println("Total teams qualified for Champions League: " + finalUclTeams.size());
        finalUclTeams.forEach(t -> System.out.println("- " + t.name));
        System.out.println("\nTotal teams qualified for Europa League: " + finalUelTeams.size());
        finalUelTeams.forEach(t -> System.out.println("- " + t.name));
        System.out.println("\nTotal teams qualified for Europa Conference League: " + finalUeclTeams.size());
        finalUeclTeams.forEach(t -> System.out.println("- " + t.name));

        // --- 3. SIMULATE THE CHAMPIONS LEAGUE ---
        if (!finalUclTeams.isEmpty()) {
            ChampionsLeague championsLeague = new ChampionsLeague(finalUclTeams);
            championsLeague.simulate();
        } else {
            System.out.println("\nNo teams qualified for the Champions League simulation.");
        }

        // --- 4. SIMULATE THE EUROPA LEAGUE ---
        if (!finalUelTeams.isEmpty()) {
            EuropaLeague europaLeague = new EuropaLeague(finalUelTeams);
            europaLeague.simulate();
        } else {
            System.out.println("\nNo teams qualified for the Europa League simulation.");
        }

        // --- 5. SIMULATE THE EUROPA CONFERENCE LEAGUE ---
        if (!finalUeclTeams.isEmpty()) {
            EuropaConferenceLeague europaConferenceLeague = new EuropaConferenceLeague(finalUeclTeams);
            europaConferenceLeague.simulate();
        } else {
            System.out.println("\nNo teams qualified for the Europa Conference League simulation.");
        }

        System.out.println("\n\n--- FULL EUROPEAN SIMULATION COMPLETE ---");
    }

    // Helper method to distribute teams to European competitions based on merit
    // (Elo rating)
    private static void distributeEuropeanTeams(
            Premier_League premierLeague, LaLiga_League laLiga, Bundesliga_League bundesliga,
            SerieA_League serieA, Ligue1_League ligue1, Eredivisie_League eredivisie,
            LigaPortugal_League ligaPortugal, SwissSuperLeague_League swissSuperLeague,
            BelgianProLeague_League belgianProLeague, AustrianBundesliga_League austrianBundesliga,
            ScottishPremiership_League scottishPremiership, TurkishSuperLig_League turkishSuperLig,
            List<Team> finalUclTeams, List<Team> finalUelTeams, List<Team> finalUeclTeams) {

        Set<Team> allEuropeanParticipantsSet = new HashSet<>(); // Use a Set to ensure uniqueness
        List<Team> potentialEuropeanTeams = new ArrayList<>();

        // Helper Consumer to add teams to the potential pool, preventing duplicates
        Consumer<List<Team>> addTeamsToPotentialPool = (teamList) -> {
            for (Team team : teamList) {
                if (allEuropeanParticipantsSet.add(team)) { // add returns true if element was new
                    potentialEuropeanTeams.add(team);
                }
            }
        };

        // Add all teams that initially qualified for ANY European competition from
        // domestic leagues
        addTeamsToPotentialPool.accept(premierLeague.getUclTeams());
        addTeamsToPotentialPool.accept(premierLeague.getUelTeams());
        addTeamsToPotentialPool.accept(premierLeague.getUeclTeams());

        addTeamsToPotentialPool.accept(laLiga.getUclTeams());
        addTeamsToPotentialPool.accept(laLiga.getUelTeams());
        addTeamsToPotentialPool.accept(laLiga.getUeclTeams());

        addTeamsToPotentialPool.accept(bundesliga.getUclTeams());
        addTeamsToPotentialPool.accept(bundesliga.getUelTeams());
        addTeamsToPotentialPool.accept(bundesliga.getUeclTeams());

        addTeamsToPotentialPool.accept(serieA.getUclTeams());
        addTeamsToPotentialPool.accept(serieA.getUelTeams());
        addTeamsToPotentialPool.accept(serieA.getUeclTeams());

        addTeamsToPotentialPool.accept(ligue1.getUclTeams());
        addTeamsToPotentialPool.accept(ligue1.getUelTeams());
        addTeamsToPotentialPool.accept(ligue1.getUeclTeams());

        addTeamsToPotentialPool.accept(eredivisie.getUclTeams());
        addTeamsToPotentialPool.accept(eredivisie.getUelTeams());
        addTeamsToPotentialPool.accept(eredivisie.getUeclTeams());

        addTeamsToPotentialPool.accept(ligaPortugal.getUclTeams());
        addTeamsToPotentialPool.accept(ligaPortugal.getUelTeams());
        addTeamsToPotentialPool.accept(ligaPortugal.getUeclTeams());

        addTeamsToPotentialPool.accept(swissSuperLeague.getUclTeams());
        addTeamsToPotentialPool.accept(swissSuperLeague.getUelTeams());
        addTeamsToPotentialPool.accept(swissSuperLeague.getUeclTeams());

        addTeamsToPotentialPool.accept(belgianProLeague.getUclTeams());
        addTeamsToPotentialPool.accept(belgianProLeague.getUelTeams());
        addTeamsToPotentialPool.accept(belgianProLeague.getUeclTeams());

        addTeamsToPotentialPool.accept(austrianBundesliga.getUclTeams());
        addTeamsToPotentialPool.accept(austrianBundesliga.getUelTeams());
        addTeamsToPotentialPool.accept(austrianBundesliga.getUeclTeams());

        addTeamsToPotentialPool.accept(scottishPremiership.getUclTeams());
        addTeamsToPotentialPool.accept(scottishPremiership.getUelTeams());
        addTeamsToPotentialPool.accept(scottishPremiership.getUeclTeams());

        addTeamsToPotentialPool.accept(turkishSuperLig.getUclTeams());
        addTeamsToPotentialPool.accept(turkishSuperLig.getUelTeams());
        addTeamsToPotentialPool.accept(turkishSuperLig.getUeclTeams());

        // Sort all potential European teams by Elo rating (highest Elo gets priority
        // for higher competition)
        potentialEuropeanTeams.sort(Comparator.comparingDouble(Team::getEloRating).reversed());

        // Distribute to competitions based on tiers and caps
        int currentTeamIndex = 0;

        // Champions League (Cap 36)
        while (finalUclTeams.size() < 36 && currentTeamIndex < potentialEuropeanTeams.size()) {
            finalUclTeams.add(potentialEuropeanTeams.get(currentTeamIndex++));
        }

        // Europa League (Cap 16)
        while (finalUelTeams.size() < 16 && currentTeamIndex < potentialEuropeanTeams.size()) {
            finalUelTeams.add(potentialEuropeanTeams.get(currentTeamIndex++));
        }

        // Europa Conference League (Cap 16)
        while (finalUeclTeams.size() < 16 && currentTeamIndex < potentialEuropeanTeams.size()) {
            finalUeclTeams.add(potentialEuropeanTeams.get(currentTeamIndex++));
        }

        // Debug output to confirm distribution
        System.out.println("\n--- DEBUG: European Team Distribution ---");
        System.out.println("DEBUG: Total potential European teams: " + potentialEuropeanTeams.size());
        System.out.println("DEBUG: Final UCL teams allocated: " + finalUclTeams.size());
        System.out.println("DEBUG: Final UEL teams allocated: " + finalUelTeams.size());
        System.out.println("DEBUG: Final UECL teams allocated: " + finalUeclTeams.size());
        System.out.println("------------------------------------------");
    }
}