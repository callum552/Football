/**
 * Represents an individual player in the simulation.
 * This class holds all the attributes that define a player's quality and role.
 */
public class Player {
    String name;
    String position; // e.g., "GK", "DC", "MC", "ST"
    int currentAbility; // Overall rating, 1-200

    // Key technical attributes, 1-20 scale
    int finishing;
    int tackling;
    int passing;
    int heading;
    int dribbling;

    // Key physical attributes, 1-20 scale
    int pace;
    int strength;

    /**
     * Constructor for a new Player.
     * @param name Player's name.
     * @param position The primary position they play.
     * @param currentAbility Their overall current ability level.
     * @param finishing Skill at scoring goals.
     * @param tackling Skill at winning the ball.
     * @param passing Skill at passing to teammates.
     */
    public Player(String name, String position, int currentAbility, int finishing, int tackling, int passing) {
        this.name = name;
        this.position = position;
        this.currentAbility = currentAbility;
        this.finishing = finishing;
        this.tackling = tackling;
        this.passing = passing;
        // For simplicity, other attributes can be derived or set to a baseline
        this.pace = currentAbility / 10;
        this.strength = currentAbility / 10;
        this.heading = currentAbility / 10;
        this.dribbling = currentAbility / 10;
    }

    @Override
    public String toString() {
        return name + " (" + position + " | CA: " + currentAbility + ")";
    }
}

