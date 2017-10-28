/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.examples;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.UtilizationModel;

/**
 * The UtilizationModelFull class is a simple model, according to which a Cloudlet always utilizes
 * a given allocated resource at 100%, all the time.
 * 
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 2.0
 */
public class UtilizationModelAccelerate implements UtilizationModel {

	final double utilizationRate = 1.5;

	/**
	 * Gets the utilization percentage of a given resource
         * in relation to the total capacity of that resource allocated
         * to the cloudlet.
         * @param time the time to get the resource usage, that isn't considered
         * for this UtilizationModel.
	 * @return Always return 1 (100% of utilization), independent of the time.
	 */
	@Override
	public double getUtilization(double time) {
		double cpuUtilization = 0.2;
		for (int i = 0; i < Math.floor(time/10); i ++) {
			cpuUtilization += cpuUtilization * utilizationRate;
			if (cpuUtilization > 1 - (1e-3))
				break;
		}
		return Math.min(1, cpuUtilization);
	}

}
