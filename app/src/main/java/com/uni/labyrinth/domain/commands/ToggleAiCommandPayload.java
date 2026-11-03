package com.uni.labyrinth.domain.commands;

import com.uni.labyrinth.domain.enums.CommandType;
import com.uni.labyrinth.model.Coordinates;

import static com.uni.labyrinth.domain.enums.CommandType.TOGGLE_AI;

public class ToggleAiCommandPayload {
        private CommandType type = TOGGLE_AI;
        private boolean enabled;

        public ToggleAiCommandPayload(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }