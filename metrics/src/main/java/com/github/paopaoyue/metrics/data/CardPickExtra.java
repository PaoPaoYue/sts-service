package com.github.paopaoyue.metrics.data;

import java.util.List;

public class CardPickExtra {
        private List<String> picked;        // unique_id of other cards in the same pick which are picked
        private List<String> unpicked;      // unique_id of other cards in the same pick which are not picked

        public CardPickExtra(List<String> picked, List<String> unpicked) {
            this.picked = picked;
            this.unpicked = unpicked;
        }

        public List<String> getPicked() {
            return picked;
        }

        public void setPicked(List<String> picked) {
            this.picked = picked;
        }

        public List<String> getUnpicked() {
            return unpicked;
        }

        public void setUnpicked(List<String> unpicked) {
            this.unpicked = unpicked;
        }
    }
