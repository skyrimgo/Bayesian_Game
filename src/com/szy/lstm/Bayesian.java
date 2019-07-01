package com.szy.lstm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Bayesian
 *
 * double v_b_min = 0.15; double v_b_max = 0.21; double p_s_min = 0.16;//
 * 用户推测发电侧电价 double p_s_max = 0.185;
 * 
 * double v_s = 0.18;// 0.17 卖方对电力的估价 double v_s_min = 0.16; double v_s_max =
 * 0.22;
 * 
 * double p_b_min = 0.16;// 发电侧推测用户电价 double p_b_max = 0.18;
 */
public class Bayesian {

    public static void main(String[] args) throws NumberFormatException, IOException {
        double v_b = 0.175;// 0.175 买方对电力的估价
        // double v_b_min = 0.15;
        double v_b_max = 0.21;
        // double p_s_min = 0.16;// 用户推测发电侧电价
        double p_s_max = 0.185;
        double v_s = 0.17;// 0.17 卖方对电力的估价
        double v_s_min = 0.16;
        // double v_s_max = 0.22;
        // double p_b_min = 0.16;// 发电侧推测用户电价
        double p_b_max = 0.18;
        double p_b;
        double p_s;
        double delta_b;
        double delta_s;
        double p_b_re = 2.0 / 3 * v_b + 1.0 / 21 * (v_b_max + 6 * v_s_min);
        double p_s_re = 2.0 / 3 * v_s + 1.0 / 21 * (6 * v_b_max + v_s_min);
        p_b = p_b_re;
        p_s = p_s_re;
        System.out.println(p_b);
        System.out.println(p_s);
        int count = 0;
        List<Double> p_b_pre_list = new ArrayList<>();
        List<Double> p_s_pre_list = new ArrayList<>();
        while (p_b < p_s && count < 15) {
            System.out.println("第" + (count + 1) + "次博弈");
            delta_b = ((p_s_max + v_b) / (2 * p_b) - 1.0) / 10;
            delta_s = (1.0 - (p_b_max + v_s) / (2 * p_s)) / 10;
            System.out.println("delta_b:" + delta_b);
            System.out.println("delta_s:" + delta_s);
            p_b *= (1 + delta_b);
            p_s *= (1 - delta_s);
            System.out.println(p_b);
            p_b_pre_list.add(p_b);
            System.out.println(p_s);
            p_s_pre_list.add(p_s);
            count++;
        }
        System.out.println("博弈次数:" + count);
        double[] p_b_pre_arr = new double[p_b_pre_list.size()];
        for (int i = 0; i < p_b_pre_list.size(); i++) {
            p_b_pre_arr[i] = p_b_pre_list.get(i);
        }
        double[] p_s_pre_arr = new double[p_s_pre_list.size()];
        for (int i = 0; i < p_s_pre_list.size(); i++) {
            p_s_pre_arr[i] = p_s_pre_list.get(i);
        }
        LSTM lstm_b = new LSTM(100, 50, 0.01);
        String[] b_res = lstm_b.run(p_b_pre_arr);
        System.out.println(String.join(",", b_res));

        LSTM lstm_s = new LSTM(100, 50, 0.01);
        String[] s_res = lstm_s.run(p_s_pre_arr);
        System.out.println(String.join(",", s_res));

        System.out.println("成交电价：" + (p_b + p_s) / 2);
        count = 0;
        p_b = p_b_re;
        p_s = p_s_re;
        while (p_b < p_s && count < 100) {
            System.out.println("第" + (count + 1) + "次博弈");
            delta_b = ((p_s_max + v_b) / (Double.parseDouble(b_res[count + 1])) - 1.0) / 100;
            delta_s = (1.0 - (p_b_max + v_s) / (2 * Double.parseDouble(s_res[count + 1]))) / 100;
            System.out.println("delta_b:" + delta_b);
            System.out.println("delta_s:" + delta_s);
            p_b *= (1 + delta_b);
            p_s *= (1 - delta_s);
            System.out.println(p_b);
            System.out.println(p_s);
            count++;
        }
        System.out.println("博弈次数:" + count);
        System.out.println("成交电价：" + (p_b + p_s) / 2);
    }

}
