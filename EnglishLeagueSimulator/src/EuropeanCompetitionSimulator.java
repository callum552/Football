import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The main entry point for the entire European Football Simulation.
 * This class orchestrates the simulation of all major domestic leagues
 * and then uses their results to simulate the UEFA Champions League.
 */
public class EuropeanCompetitionSimulator {

    public static void main(String[] args) {
        // --- 1. SIMULATE ALL DOMESTIC LEAGUES ---
        System.out.println("--- STARTING DOMESTIC LEAGUE SIMULATIONS ---");

        Premier_League premierLeague = new Premier_League();
        premierLeague.main(null); // Run the full simulation

        LaLiga_League laLiga = new LaLiga_League();
        laLiga.main(null);

        Bundesliga_League bundesliga = new Bundesliga_League();
        bundesliga.main(null);

        SerieA_League serieA = new SerieA_League();
        serieA.main(null);

        Ligue1_League ligue1 = new Ligue1_League();
        ligue1.main(null);

        Eredivisie_League eredivisie = new Eredivisie_League();
        eredivisie.main(null);

        LigaPortugal_League ligaPortugal = new LigaPortugal_League();
        ligaPortugal.main(null);

        // --- 2. GATHER QUALIFIED TEAMS FOR CHAMPIONS LEAGUE ---
        System.out.println("\n\n--- GATHERING TEAMS FOR EUROPEAN COMPETITIONS ---");

        List<Team> uclTeams = new ArrayList<>();
        uclTeams.addAll(premierLeague.getUclTeams());
        uclTeams.addAll(laLiga.getUclTeams());
        uclTeams.addAll(bundesliga.getUclTeams());
        uclTeams.addAll(serieA.getUclTeams());
        uclTeams.addAll(ligue1.getUclTeams());
        uclTeams.addAll(eredivisie.getUclTeams());
        uclTeams.addAll(ligaPortugal.getUclTeams());

        System.out.println("Total teams qualified for Champions League: " + uclTeams.size());
        uclTeams.forEach(t -> System.out.println("- " + t.name));

        // In a full simulation, you would also gather UEL and UECL teams here.

        // --- 3. SIMULATE THE CHAMPIONS LEAGUE ---
        if (!uclTeams.isEmpty()) {
            ChampionsLeague championsLeague = new ChampionsLeague(uclTeams);
            championsLeague.simulate();
        } else {
            System.out.println("\nNo teams qualified for the Champions League simulation.");
        }

        System.out.println("\n\n--- FULL EUROPEAN SIMULATION COMPLETE ---");
    }
}
