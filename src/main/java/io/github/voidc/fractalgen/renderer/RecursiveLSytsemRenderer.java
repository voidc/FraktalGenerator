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
                        renderRule(lSystem, lSystem.getRules().get(rule.charAt(i)), depth + 1);
                    } else {
                        line();
                    }
            }
        }
    }
}
