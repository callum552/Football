import java.util.Random;

public class MatchSimulator {
    protected final Random random = new Random();

    /**
     * NEW METHOD: Simulates a match and returns the updated Match object with
     * scores.
     * This is required for leagues with head-to-head tie-breakers.
     * 
     * @param match The match to be simulated.
     * @return The same Match object, now populated with the result.
     */
    public Match simulateMatch(Match match) {
        double homeLambda = calculateLambda(match.homeTeam, match.awayTeam, 1.25);
        double awayLambda = calculateLambda(match.awayTeam, match.homeTeam, 1.0);
        int homeGoals = getPoisson(homeLambda);
        int awayGoals = getPoisson(awayLambda);

        // Populate the match object with the results
        match.homeGoals = homeGoals;
        match.awayGoals = awayGoals;

        // Update stats and Elo ratings
        updateEloRatings(match.homeTeam, match.awayTeam, homeGoals, awayGoals);
        match.homeTeam.recordMatchResult(homeGoals, awayGoals);
        match.awayTeam.recordMatchResult(awayGoals, homeGoals);

        return match;
    }

    public void simulateSingleMatch(Team team1, Team team2, boolean updateStats) {
        double lambda1 = calculateLambda(team1, team2, 1.25); // Assume home advantage for team1
        double lambda2 = calculateLambda(team2, team2, 1.0); // No home advantage for away team
        int goals1 = getPoisson(lambda1);
        int goals2 = getPoisson(lambda2);

        if (updateStats) {
            team1.recordMatchResult(goals1, goals2);
            team2.recordMatchResult(goals2, goals1);
        }
        updateEloRatings(team1, team2, goals1, goals2);
    }

    public Team simulateSingleMatch(Team team1, Team team2) {
        double lambda1 = calculateLambda(team1, team2, 1.0); // Neutral venue
        double lambda2 = calculateLambda(team2, team1, 1.0);
        int goals1 = getPoisson(lambda1);
        int goals2 = getPoisson(lambda2);

        System.out.printf("%s %d - %d %s", team1.name, goals1, goals2, team2.name);
        if (goals1 == goals2) {
            return resolveWithPenalties(team1, team2);
        } else {
            System.out.println("");
            updateEloRatings(team1, team2, goals1, goals2);
            return goals1 > goals2 ? team1 : team2;
        }
    }

    protected Team resolveWithPenalties(Team team1, Team team2) {
        int team1Pens = random.nextInt(6);
        int team2Pens = random.nextInt(6);
        while (team1Pens == team2Pens) {
            team1Pens += random.nextInt(2);
            team2Pens += random.nextInt(2);
        }
        System.out.printf(" (%s wins %d-%d on penalties)\n", (team1Pens > team2Pens ? team1.name : team2.name),
                team1Pens, team2Pens);
        // Elo update for a draw
        updateEloRatings(team1, team2, 0, 0);
        return team1Pens > team2Pens ? team1 : team2;
    }

    protected double calculateLambda(Team attackingTeam, Team defendingTeam, double advantageScaler) {
        double baseLambda = 1.3;
        double strengthRatio = (double) attackingTeam.getAttackStrength() / (double) defendingTeam.getDefenceStrength();
        double strengthFactor = Math.pow(strengthRatio, 1.5);
        double eloDifference = attackingTeam.getEloRating() - defendingTeam.getEloRating();
        // Increased eloFactor multiplier from 0.4 to 0.6
        double eloFactor = 1 + (eloDifference / 400.0) * 0.6;
        double finalLambda = baseLambda * strengthFactor * eloFactor * advantageScaler;
        finalLambda *= (1 + (random.nextGaussian() * 0.02));
        return finalLambda;
    }

    protected int getPoisson(double lambda) {
        if (lambda <= 0)
            return 0;
        double l = Math.exp(-lambda);
        int k = 0;
        double p = 1.0;
        do {
            k++;
            p *= random.nextDouble();
        } while (p > l);
        return k - 1;
    }

    protected void updateEloRatings(Team team1, Team team2, int goals1, int goals2) {
        double result1 = 0.5;
        if (goals1 > goals2)
            result1 = 1.0;
        if (goals1 < goals2)
            result1 = 0.0;
        double expected1 = 1.0 / (1.0 + Math.pow(10, (team2.getEloRating() - team1.getEloRating()) / 400.0));
        // Changed K-factor from 30 to 20
        team1.setEloRating(team1.getEloRating() + 20 * (result1 - expected1));
        team2.setEloRating(team2.getEloRating() + 20 * ((1 - result1) - (1 - expected1)));
    }
}