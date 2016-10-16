package io.github.voidc.fractalgen.renderer;

import io.github.voidc.fractalgen.LSystem;

public class RecursiveLSytsemRenderer extends LSystemRenderer {

    @Override
    protected void renderRule(LSystem lSystem, String rule, int depth) {
        for (int i = 0; i < rule.length(); i++) {
            switch (rule.charAt(i)) {
                case '+':
                    angle += lSystem.getAngle();
                    continue;
                case '-':
                    angle -= lSystem.getAngle();
                    continue;
                default:
                    if (depth < maxDepth) {
                        char symbol = rule.charAt(i);
                        String newRule = lSystem.getRules().get(symbol);
                        if(newRule.length() == 1 && newRule.charAt(0) == symbol)
                            line();
                        else
                            renderRule(lSystem, newRule, depth + 1);
                    } else {
                        line();
                    }
            }
        }
    }
}
