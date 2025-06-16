public class Match {
    Team homeTeam;
    Team awayTeam;
    int homeGoals; // Package-private is fine
    int awayGoals; // Package-private is fine

    public Match(Team homeTeam, Team awayTeam) {
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.homeGoals = -1; // Indicates match not yet played
        this.awayGoals = -1;
    }
}
