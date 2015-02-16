/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.nmmst.test;

import java.io.IOException;
import net.nmmst.tools.ProjectorUtil;

/**
 *
 * @author Tsai ChiaPing <chia7712@gmail.com>
 */
public class ProjectorTest {
    public static void main(String[] args) throws IOException {
        String arg = args[0];
        if (arg.compareToIgnoreCase("on") == 0) {
            ProjectorUtil.switchAllMachine(true);
            System.out.println("lamp on");
        } else if (arg.compareToIgnoreCase("off") == 0) {
            ProjectorUtil.switchAllMachine(false);
            System.out.println("lamp off");
        } else if (arg.compareToIgnoreCase("discovery") == 0) {
            System.out.println(ProjectorUtil.discoverDevices());
        }
    }
}
