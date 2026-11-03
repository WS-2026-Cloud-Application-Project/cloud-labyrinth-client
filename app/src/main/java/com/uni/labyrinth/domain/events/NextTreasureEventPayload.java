package com.uni.labyrinth.domain.events;


import com.uni.labyrinth.model.Treasure;

public class NextTreasureEventPayload {
        private String type; // CommandType als String (z.B. "CONNECT")
        private Treasure treasure;


        // Getter & Setter
        public String getType() { return type;}
        public Treasure getTreasure() {return treasure;}



    }
