package org.arquillian.smart.testing.configuration;

import static org.arquillian.smart.testing.scm.ScmRunnerProperties.HEAD;

public class Scm {

    private Range range;

    public Range getRange() {
        return range;
    }

    public void setRange(Range range) {
        this.range = range;
    }

    public static Builder builder() {
        return new Scm.Builder();
    }

    public static class Builder {
        private Range range;

        public Builder range(Range range) {
            this.range = range;
            return this;
        }

        public Builder lastChanges(String n) {
            this.range = Range.builder()
                            .head(HEAD)
                            .tail(String.join("~", HEAD, n))
                        .build();
            return this;
        }

        public Scm build() {
            final Scm scm = new Scm();
            scm.range = this.range;

            return scm;
        }
    }
}
