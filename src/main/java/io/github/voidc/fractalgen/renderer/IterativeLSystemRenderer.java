package io.github.voidc.fractalgen.renderer;

import io.github.voidc.fractalgen.LSystem;

import java.util.ArrayList;
import java.util.List;

public class IterativeLSystemRenderer extends LSystemRenderer {
    private List<String> results = new ArrayList<>();

    @Override
    protected void renderRule(LSystem lSystem, String rule, int depth) {
        results.add(lSystem.getAxiom());

        for(int d = results.size(); d < depth; d++) {
            String previous = results.get(d - 1);
            StringBuilder builder = new StringBuilder();

            for(int i = 0; i < previous.length(); i++) {
                builder.append(lSystem.getRules().get(previous.charAt(i)));
            }

            results.add(builder.toString());
        }

        drawSimplified(lSystem, results.get(depth - 1));
    }

    private void drawSimplified(LSystem lSystem, String rule) {
        for(int i = 0; i < rule.length(); i++) {
            switch (rule.charAt(i)) {
                case '+':
                    angle += lSystem.getAngle();
                    continue;
                case '-':
                    angle -= lSystem.getAngle();
                    continue;
                default:
                    line();
            }
        }
    }

    @Override
    public void renderToCache(LSystem lSystem) {
        results.clear();
        super.renderToCache(lSystem);
    }
}
