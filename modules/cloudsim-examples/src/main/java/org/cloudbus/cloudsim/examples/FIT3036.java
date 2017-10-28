/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation
 *               of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.examples;


import org.apache.commons.lang3.StringUtils;
import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.*;
import org.cloudbus.cloudsim.power.models.PowerModelSpecPowerHpProLiantMl110G4Xeon3040;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

import java.text.DecimalFormat;
import java.util.*;


/**
 * A simple example showing how to create
 * a datacenter with one host and run two
 * cloudlets on it. The cloudlets run in
 * VMs with the same MIPS requirements.
 * The cloudlets will take the same time to
 * complete the execution.
 */
public class FIT3036 {
    
    /**
     * The cloudlet list.
     */
    private static List<Cloudlet> cloudletList;
    
    /**
     * The vmlist.
     */
    private static List<PowerVm> vmlist;
    
    // number of cloudlets (i.e. number of jobs)
    static int NO_CLOUDLETS = 100;
    // number of Virtual machines
    static int NO_VMS       = 3;
    // number of hosts (physical machines)
    static int NO_HOSTS     = 3;
    
    /**
     * Creates main() to run this example
     */
    public static void main(String[] args) {
        
        Log.printLine("Starting FIT3036...");
        
        try {
            // First step: Initialize the CloudSim package. It should be called
            // before creating any entities.
            int      num_user   = 1;   // number of cloud users
            Calendar calendar   = Calendar.getInstance();
            boolean  trace_flag = false;  // mean trace events
            
            // Initialize the CloudSim library
            CloudSim.init(num_user, calendar, trace_flag);
            
            // Second step: Create Datacenters
            //Datacenters are the resource providers in CloudSim. We need at list one of them to run a CloudSim simulation
            @SuppressWarnings("unused")
            PowerDatacenter datacenter0 = createDatacenter("Datacenter_0");
            datacenter0.setDisableMigrations(false);
            
            //Third step: Create Broker
            DatacenterBroker broker   = createBroker();
            int              brokerId = broker.getId();
            
            //Fourth step: Create one virtual machine
            vmlist = new ArrayList<PowerVm>();
            //1234 is the seed to get the same random sequence each run.
            // I am going to create three types of VMs: small, medium, and large
            //VM description

            int    mips      = 100;
            long   size      = 10000; //image size (MB)
            int    ram       = 512; //vm memory (MB)
            long   bw        = 1000;
            int    pesNumber = 1; //number of cpus
            String vmm       = "Xen"; //VMM name
            for (int i = 0; i < NO_VMS; i++) {
                // The type of the VM is selected randomly
                // you can use the modules operation to create fixed size sets
                // three is the number of types
                int vmid = i;
                PowerVm vm = new PowerVm(
                        i,
                        brokerId,
                        mips,
                        pesNumber,
                        ram,
                        bw,
                        size,
                        1,
                        vmm,
                        new CloudletSchedulerDynamicWorkload(100, 1),
                        10
                );
                //add the VM to the vmList
                vmlist.add(vm);
            }
            
            //submit vm list to the broker
            broker.submitVmList(vmlist);
            
            
            //Fifth step: Create two Cloudlets
            cloudletList = new ArrayList<Cloudlet>();
            
            //Cloudlet properties
            int id = 0;
            // this variable has been renamed
            // create three types of cloudlets, small, medium and large
            int              pesNumberCL        = 1;
            long             lengthCL         = 100;
            long             fileSizeCL       = 300;
            long             outputSizeCL     = 300;
            for (int i = 0; i < NO_CLOUDLETS; i++) {
                Cloudlet cloudlet = new Cloudlet(
                        i,
                        lengthCL,
                        pesNumberCL,
                        fileSizeCL,
                        outputSizeCL,
                        new UtilizationModelAccelerate(),
                        new UtilizationModelAccelerate(),
                        new UtilizationModelAccelerate()
                );
                cloudlet.setUserId(brokerId);
                //add the cloudlets to the list
                cloudletList.add(cloudlet);
            }
            
            
            //submit cloudlet list to the broker
            broker.submitCloudletList(cloudletList);
            
            
            //bind the cloudlets to the vms. This way, the broker
            // will submit the bound cloudlets only to the specific VM
//            broker.bindCloudletToVm(cloudlet1.getCloudletId(), vm1.getId());
//            broker.bindCloudletToVm(cloudlet2.getCloudletId(), vm2.getId());
            
            // Sixth step: Starts the simulation
            CloudSim.startSimulation();
            
            
            // Final step: Print results when simulation is over
            List<Cloudlet> newList = broker.getCloudletReceivedList();
            Log.printLine("Number of cloudlets received: " + String.valueOf(newList.size()));
            
            CloudSim.stopSimulation();
            
            printCloudletList(newList);
            
            Log.printLine("FIT3036 finished!");
        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("The simulation has been terminated due to an unexpected error");
        }
    }
    
    private static PowerDatacenter createDatacenter(String name) {
        
        // Here are the steps needed to create a PowerDatacenter:
        // 1. We need to create a list to store
        //    our machine
        // 2. A Machine contains one or more PEs or CPUs/Cores.
        // In this example, it will have only one core.
        // need to store Pe id and MIPS Rating
        
        //4. Create Host with its id and list of PEs and add them to the list of machines
        int  hostId  = 0;
        // 16GB of ram for each machine
        int  ram     = 16 * 1024; //host memory (MB)
        long storage = 10000000; //host storage
        int  bw      = 100000;
        // use i as a hostId
        //all physical machines are identical
        List<PowerHost> hostList = new ArrayList<>();
        for (int id = 1; id <= NO_HOSTS; id ++) {
            List<Pe> peList = new ArrayList<>();
            for (int peId = 1; peId <= 2; peId ++) {
                peList.add(new Pe(
                        peId,
                        new PeProvisionerSimple(100)
                ));
            }
            hostList.add(new PowerHostUtilizationHistory(
                    id,
                    new RamProvisionerSimple(3000),
                    new BwProvisionerSimple(3000),
                    30000,
                    peList,
                    new VmSchedulerTimeSharedOverSubscription(peList),
                    new PowerModelSpecPowerHpProLiantMl110G4Xeon3040()
            ));
        }
        PowerVmAllocationPolicyMigrationMini_MU vmAllocationPolicy =
                new PowerVmAllocationPolicyMigrationMini_MU(hostList);

        // 5. Create a DatacenterCharacteristics object that stores the
        //    properties of a data center: architecture, OS, list of
        //    Machines, allocation policy: time- or space-shared, time zone
        //    and its price (G$/Pe time unit).
        String              arch           = "x86";      // system architecture
        String              os             = "Linux";          // operating system
        String              vmm            = "Xen";
        double              time_zone      = 10.0;         // time zone this resource located
        double              cost           = 3.0;              // the cost of using processing in this resource
        double              costPerMem     = 0;        // the cost of using memory in this resource
        double              costPerStorage = 0;    // the cost of using storage in this resource
        double              costPerBw      = 0;            // the cost of using bw in this resource
        LinkedList<Storage> storageList    = new LinkedList<Storage>();    //we are not adding SAN devices by now
        
        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);
        
        
        // 6. Finally, we need to create a PowerDatacenter object.
        PowerDatacenter datacenter = null;
        try {
            datacenter = new PowerDatacenter(name, characteristics, vmAllocationPolicy, storageList, 10);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return datacenter;
    }
    
    //We strongly encourage users to develop their own broker policies, to submit vms and cloudlets according
    //to the specific rules of the simulated scenario
    private static DatacenterBroker createBroker() {
        
        DatacenterBroker broker = null;
        try {
            broker = new PowerDatacenterBroker("Broker");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return broker;
    }
    
    /**
     * Prints the Cloudlet objects
     *
     * @param list list of Cloudlets
     */
    private static void printCloudletList(List<Cloudlet> list) {
        int      size = list.size();
        Cloudlet cloudlet;
        
        String gap = "   ";
        String gap2 = gap + gap;
        Log.printLine();
        Log.printLine("========== OUTPUT ==========");
        Log.printLine(gap + "Cloudlet ID" + gap2 + "STATUS" + gap2 +
                      "Data center ID" + gap2 + "VM ID" + gap2 + "Time" + gap2 + "Start Time" + gap2 + "Finish Time" + gap);
        
        DecimalFormat dft = new DecimalFormat("###.##");
        for (int i = 0; i < size; i++) {
            cloudlet = list.get(i);
            StringBuilder cloudletInfo = new StringBuilder();

            int cloudletId = cloudlet.getCloudletId();
            cloudletInfo.append(StringUtils.center(String.valueOf(cloudletId), 17));

            String cloudletStatus = cloudlet.getCloudletStatusString();
            if (Cloudlet.getStatusString(Cloudlet.SUCCESS).equals(cloudletStatus)) {
                cloudletInfo.append(StringUtils.center(cloudletStatus.toUpperCase(), 12));

                int dataCenterId = cloudlet.getResourceId();
                cloudletInfo.append(StringUtils.center(String.valueOf(dataCenterId), 20));

                int vmId = cloudlet.getVmId();
                cloudletInfo.append(StringUtils.center(String.valueOf(vmId), 11));

                String time = dft.format(cloudlet.getActualCPUTime());
                cloudletInfo.append(StringUtils.center(time, 10));

                String startTime = dft.format(cloudlet.getExecStartTime());
                cloudletInfo.append(StringUtils.center(startTime, 16));

                String finshTime = dft.format(cloudlet.getFinishTime());
                cloudletInfo.append(StringUtils.center(finshTime, 17));
            }

            Log.printLine(cloudletInfo);
        }
    }
}
