package org.cloudbus.cloudsim.power;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;

import java.util.List;

public class PowerVmAllocationPolicyMigrationMini_MU extends PowerVmAllocationPolicyMigrationAbstract_MU {
    public PowerVmAllocationPolicyMigrationMini_MU(List<? extends Host> hosts) {
        super(hosts, new PowerVmSelectionPolicyMinimumUtilization());
    }

    @Override
    protected boolean isHostOverUtilized(PowerHost host) {
        addHistoryEntry(host, getUtilizationThreshold());
        double totalRequestedMips = 0;
        for (Vm vm : host.getVmList()) {
            totalRequestedMips += vm.getCurrentRequestedTotalMips();
        }
        double utilization = totalRequestedMips / host.getTotalMips();
        return utilization > getUtilizationThreshold();
    }

    protected double getUtilizationThreshold() {return 0.7;}
}
