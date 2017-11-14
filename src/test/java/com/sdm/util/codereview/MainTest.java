package com.sdm.util.codereview;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MainTest {
    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void main() throws Exception {
        assertEquals(2, 1 + 1);
        System.out.println("main");
        String[] args = {"./target/test-classes/report-dir-1"};
        Main.main(args);
    }
}