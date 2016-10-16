package io.github.voidc.fractalgen;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class LSystem {
    private String name;
    private String axiom;
    private Map<Character, String> rules;
    private double angle;

    public static final String CONSTANTS = "+-";

    public LSystem(String name) {
        this.name = name;
        this.rules = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAxiom() {
        return axiom;
    }

    public void setAxiom(String axiom) {
        this.axiom = axiom;
    }

    public Map<Character, String> getRules() {
        return rules;
    }

    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }

    @Override
    public String toString() {
        return name;
    }

    public boolean isRuleUsed(char ruleSymbol) {
        if(axiom.indexOf(ruleSymbol) != -1)
            return true;

        for(char s : rules.keySet()) {
            if(s != ruleSymbol && rules.get(s).indexOf(ruleSymbol) != -1)
                return true;
        }

        return false;
    }

    @Override
    protected LSystem clone() {
        LSystem ls = new LSystem(name);
        ls.setAxiom(axiom);
        rules.forEach((symbol, rule) -> ls.getRules().put(symbol, rule));
        ls.setAngle(angle);
        return ls;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        LSystem lSystem = (LSystem) o;

        if (Double.compare(lSystem.angle, angle) != 0)
            return false;
        if (!name.equals(lSystem.name))
            return false;
        if (!axiom.equals(lSystem.axiom))
            return false;
        return rules.equals(lSystem.rules);

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = name.hashCode();
        result = 31 * result + axiom.hashCode();
        result = 31 * result + rules.hashCode();
        temp = Double.doubleToLongBits(angle);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    public static LSystem[] presets = new LSystem[] {
            koch(),
            sierpinski(),
            hilbert(),
            peano(),
            dragon(),
            empty()
    };

    public static LSystem koch() {
        LSystem koch = new LSystem("Koch-Kurve");
        koch.setAxiom("F");
        koch.getRules().put('F', "F-F++F-F");
        koch.setAngle(Math.toRadians(60));
        return koch;
    }

    public static LSystem sierpinski() {
        LSystem sirpinski = new LSystem("Sierpinski-Dreieck");
        sirpinski.setAxiom("A+A+B");
        sirpinski.getRules().put('A', "AA");
        sirpinski.getRules().put('B', "B+A-B-A+B");
        sirpinski.setAngle(Math.toRadians(120));
        return sirpinski;
    }

    public static LSystem hilbert() {
        LSystem hilbert = new LSystem("Hilbert-Kurve");
        hilbert.setAxiom("A");
        hilbert.getRules().put('A', "-BC+ACA+CB-");
        hilbert.getRules().put('B', "+AC-BCB-CA+");
        hilbert.getRules().put('C', "C");
        hilbert.setAngle(Math.toRadians(90));
        return hilbert;
    }

    public static LSystem peano() {
        LSystem peano = new LSystem("Peano-Kurve");
        peano.setAxiom("A");
        peano.getRules().put('A', "ACBCA+C+BCACB-C-ACBCA");
        peano.getRules().put('B', "BCACB-C-ACBCA+C+BCACB");
        peano.getRules().put('C', "C");
        peano.setAngle(Math.toRadians(90));
        return peano;
    }

    public static LSystem dragon() {
        LSystem dragon = new LSystem("Drachenkurve");
        dragon.setAxiom("A");
        dragon.getRules().put('A', "A+BC+");
        dragon.getRules().put('B', "-CA-B");
        dragon.getRules().put('C', "C");
        dragon.setAngle(Math.toRadians(90));
        return dragon;
    }

    public static LSystem empty() {
        LSystem empty = new LSystem("Leere Vorlage");
        empty.setAxiom("");
        empty.setAngle(Math.toRadians(90));
        return empty;
    }
}
