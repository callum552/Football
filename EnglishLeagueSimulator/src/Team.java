public class Team {
    String name;
    int attackStrength;
    int defenceStrength;
    double eloRating;

    // Domestic stats
    int gamesPlayed;
    int wins;
    int draws;
    int losses;
    int goalsFor;
    int goalsAgainst;
    int goalDifference;
    int points;

    public Team(String name, int attackStrength, int defenceStrength, double initialElo) {
        this.name = name;
        this.attackStrength = attackStrength;
        this.defenceStrength = defenceStrength;
        this.eloRating = initialElo;
    }

    public void recordMatchResult(int goalsScored, int goalsConceded) {
        this.gamesPlayed++;
        this.goalsFor += goalsScored;
        this.goalsAgainst += goalsConceded;
        this.goalDifference = this.goalsFor - this.goalsAgainst;

        if (goalsScored > goalsConceded) {
            this.wins++;
            this.points += 3;
        } else if (goalsScored == goalsConceded) {
            this.draws++;
            this.points += 1;
        } else {
            this.losses++;
        }
    }

    public void resetStats() {
        this.gamesPlayed = 0;
        this.wins = 0;
        this.draws = 0;
        this.losses = 0;
        this.goalsFor = 0;
        this.goalsAgainst = 0;
        this.goalDifference = 0;
        this.points = 0;
    }

    // --- GETTERS AND SETTERS ---

    public String getName() { return name; }
    public int getAttackStrength() { return attackStrength; }
    public int getDefenceStrength() { return defenceStrength; }
    public double getEloRating() { return eloRating; }
    public void setEloRating(double eloRating) { this.eloRating = eloRating; }
    public int getPoints() { return points; }
    public int getGoalDifference() { return goalDifference; }
    public int getGoalsFor() { return goalsFor; }
    public int getWins() { return wins; }


    @Override
    public String toString() {
        return this.name;
    }
}