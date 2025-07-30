package com.gamedevduo.heroarena.global.util;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class RandomUtil {
    public String generateCode() {
        Random random = new Random();
        return String.format("%05d", random.nextInt(100000));
    }
}
