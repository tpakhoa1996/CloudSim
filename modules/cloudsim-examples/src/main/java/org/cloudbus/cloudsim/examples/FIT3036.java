/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation
 *               of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.examples;


import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
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
    private static List<Vm> vmlist;
    
    // number of cloudlets (i.e. number of jobs)
    static int NO_CLOUDLETS = 100;
    // number of Virtual machines
    static int NO_VMS       = 150;
    // number of hosts (physical machines)
    static int NO_HOSTS     = 200;
    
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
            Datacenter datacenter0 = createDatacenter("Datacenter_0");
            
            //Third step: Create Broker
            DatacenterBroker broker   = createBroker();
            int              brokerId = broker.getId();
            
            //Fourth step: Create one virtual machine
            vmlist = new ArrayList<Vm>();
            //1234 is the seed to get the same random sequence each run.
            Random random = new Random(1234);
            // I am going to create three types of VMs: small, medium, and large
            //VM description
            
            // this is the old code
/*
            int    vmid      = 0;
            int    mips      = 250;
            long   size      = 10000; //image size (MB)
            int    ram       = 512; //vm memory (MB)
            long   bw        = 1000;
            int    pesNumber = 1; //number of cpus
            String vmm       = "Xen"; //VMM name
*/
            // index=0 means small VM
            // index=1 means medium size VM
            // index=2 means large size VM
            int    vmid        = 0;
            int    mips[]      = {250, 500, 1000};
            long   size[]      = {10000, 20000, 40000}; //image size (MB)
            int    ram[]       = {512, 1024, 2048}; //vm memory (MB)
            long   bw[]        = {1000, 2000, 5000};
            int    pesNumber[] = {1, 4, 8}; //number of cpus
            String vmm         = "Xen"; //VMM name
            //create two VMs
            
            Vm  vm;
            int rand;
            for (int i = 0; i < NO_VMS; i++) {
                // The type of the VM is selected randomly
                // you can use the modules operation to create fixed size sets
                // three is the number of types
                vmid = i;
                rand = random.nextInt(3);
                vm = new Vm(vmid, brokerId, mips[rand], pesNumber[rand], ram[rand], bw[rand], size[rand], vmm, new CloudletSchedulerTimeShared());
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
            int              pesNumberCL[]    = {1, 2, 4};
            long             length[]         = {250000, 500000, 1000000};
            long             fileSize[]       = {300, 600, 1200};
            long             outputSize[]     = {300, 600, 1200};
            UtilizationModel utilizationModel = new UtilizationModelFull();
            Cloudlet         cloudlet1        = null;
            for (int i = 0; i < NO_CLOUDLETS; i++) {
                rand = random.nextInt(3);
                cloudlet1 = new Cloudlet(i, length[rand], pesNumberCL[rand], fileSize[rand], outputSize[rand], utilizationModel, utilizationModel, utilizationModel);
                cloudlet1.setUserId(brokerId);
                //add the cloudlets to the list
                cloudletList.add(cloudlet1);
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
            
            CloudSim.stopSimulation();
            
            printCloudletList(newList);
            
            Log.printLine("FIT3036 finished!");
        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("The simulation has been terminated due to an unexpected error");
        }
    }
    
    private static Datacenter createDatacenter(String name) {
        
        // Here are the steps needed to create a PowerDatacenter:
        // 1. We need to create a list to store
        //    our machine
        List<Host> hostList = new ArrayList<Host>();
        
        // 2. A Machine contains one or more PEs or CPUs/Cores.
        // In this example, it will have only one core.
        List<Pe> peList = new ArrayList<Pe>();
        
        int mips[] = {1000, 1000, 1000, 1000};
        
        
        // 3. Create PEs and add these into a list.
        for (int i = 0; i < mips.length; i++) {
            peList.add(new Pe(0, new PeProvisionerSimple(mips[i])));
        }
        // need to store Pe id and MIPS Rating
        
        //4. Create Host with its id and list of PEs and add them to the list of machines
        int  hostId  = 0;
        // 16GB of ram for each machine
        int  ram     = 16 * 1024; //host memory (MB)
        long storage = 10000000; //host storage
        int  bw      = 100000;
        // use i as a hostId
        //all physical machines are identical
        for (int i = 0; i < NO_HOSTS; i++) {
            hostList.add(
                    new Host(
                            i,
                            new RamProvisionerSimple(ram),
                            new BwProvisionerSimple(bw),
                            storage,
                            peList,
                            new VmSchedulerTimeShared(peList)
                    )
            ); // This is our machine
        }
        
        // 5. Create a DatacenterCharacteristics object that stores the
        //    properties of a data center: architecture, OS, list of
        //    Machines, allocation policy: time- or space-shared, time zone
        //    and its price (G$/Pe time unit).
        String              arch           = "x86";      // system architecture
        String              os             = "Linux";          // operating system
        String              vmm            = "Xen";
        double              time_zone      = 10.0;         // time zone this resource located
        double              cost           = 3.0;              // the cost of using processing in this resource
        double              costPerMem     = 0.05;        // the cost of using memory in this resource
        double              costPerStorage = 0.001;    // the cost of using storage in this resource
        double              costPerBw      = 0.0;            // the cost of using bw in this resource
        LinkedList<Storage> storageList    = new LinkedList<Storage>();    //we are not adding SAN devices by now
        
        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);
        
        
        // 6. Finally, we need to create a PowerDatacenter object.
        Datacenter datacenter = null;
        try {
            datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
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
            broker = new DatacenterBroker("Broker");
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
        
        String indent = "    ";
        Log.printLine();
        Log.printLine("========== OUTPUT ==========");
        Log.printLine("Cloudlet ID" + indent + "STATUS" + indent +
                      "Data center ID" + indent + "VM ID" + indent + "Time" + indent + "Start Time" + indent + "Finish Time");
        
        DecimalFormat dft = new DecimalFormat("###.##");
        for (int i = 0; i < size; i++) {
            cloudlet = list.get(i);
            Log.print(indent + cloudlet.getCloudletId() + indent + indent);
            
            if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
                Log.print("SUCCESS");
                
                Log.printLine(indent + indent + cloudlet.getResourceId() + indent + indent + indent + cloudlet.getVmId() +
                              indent + indent + dft.format(cloudlet.getActualCPUTime()) + indent + indent + dft.format(cloudlet.getExecStartTime()) +
                              indent + indent + dft.format(cloudlet.getFinishTime()));
            }
        }
        
    }
}
