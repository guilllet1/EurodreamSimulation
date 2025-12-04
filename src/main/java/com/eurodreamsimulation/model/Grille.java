package com.eurodreamsimulation.model;

import java.util.List;

public class Grille {
    private final List<Integer> numeros;
    private final int numeroDream;

    public Grille(List<Integer> numeros, int numeroDream) {
        this.numeros = numeros;
        this.numeroDream = numeroDream;
    }

    public List<Integer> getNumeros() { return numeros; }
    public int getNumeroDream() { return numeroDream; }

    @Override
    public String toString() {
        return numeros + " (Dream: " + numeroDream + ")";
    }
}