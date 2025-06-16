import java.util.Random;

public class EuropeanMatchSimulator extends MatchSimulator {

    public Team simulateTwoLeggedTie(Team team1, Team team2) {
        System.out.printf("\n%s vs %s\n", team1.name, team2.name);
        // Leg 1: team1 is home
        int leg1_team1_goals = getPoisson(calculateLambda(team1, team2, 1.25));
        int leg1_team2_goals = getPoisson(calculateLambda(team2, team1, 1.0));
        System.out.printf("Leg 1: %s %d - %d %s\n", team1.name, leg1_team1_goals, leg1_team2_goals, team2.name);

        // Leg 2: team2 is home
        int leg2_team2_goals = getPoisson(calculateLambda(team2, team1, 1.25));
        int leg2_team1_goals = getPoisson(calculateLambda(team1, team2, 1.0));
        System.out.printf("Leg 2: %s %d - %d %s\n", team2.name, leg2_team2_goals, leg2_team1_goals, team1.name);

        int totalGoalsTeam1 = leg1_team1_goals + leg2_team1_goals;
        int totalGoalsTeam2 = leg1_team2_goals + leg2_team2_goals;

        System.out.printf("Aggregate: %s %d - %d %s. ", team1.name, totalGoalsTeam1, totalGoalsTeam2, team2.name);

        if (totalGoalsTeam1 > totalGoalsTeam2) {
            System.out.println(team1.name + " wins on aggregate.");
            return team1;
        } else if (totalGoalsTeam2 > totalGoalsTeam1) {
            System.out.println(team2.name + " wins on aggregate.");
            return team2;
        } else {
            // Away goals rule is removed, go straight to penalties if aggregate is level.
            return resolveWithPenalties(team1, team2);
        }
    }
}