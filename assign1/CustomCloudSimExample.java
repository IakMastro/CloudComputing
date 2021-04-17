package org.cloudbus.cloudsim.examples;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

public class CustomCloudSimExample {
    /**
     * The cloudlet list.
     */
    private static List<Cloudlet> cloudletList;

    /**
     * The vmlist.
     */
    private static List<Vm> vmlist;

    private static List<Vm> createVM(int userId, int vms, int idShift) {
        //Creates a container to store VMs. This list is passed to the broker later
        var list = new LinkedList<Vm>();

        //VM Parameters
        var size = 300; //image size (MB)
        int ram = 500; //vm memory (MB)
        int mips = 500;
        var bw = 1000;
        int pesNumber = 1; //number of cpus
        var vmm = "Xen"; //VMM name

        //create VMs
        var vm = new Vm[vms];

        for (int i = 0; i < vms; i++) {
            vm[i] = new Vm(idShift + i, userId, mips, pesNumber, ram, bw,
                    size, vmm, new CloudletSchedulerSpaceShared());
            list.add(vm[i]);
        }

        return list;
    }

    private static List<Cloudlet> createCloudlet(int userId, int cloudlets, int idShift) {
        // Creates a container to store Cloudlets
        var list = new LinkedList<Cloudlet>();

        //cloudlet parameters
        var length = 1000;
        var fileSize = 300;
        var outputSize = 300;
        int pesNumber = 1;
        var utilizationModel = new UtilizationModelFull();

        var cloudlet = new Cloudlet[cloudlets];

        for (int i = 0; i < cloudlets; i++) {
            cloudlet[i] = new Cloudlet(idShift + i, length, pesNumber, fileSize, outputSize, utilizationModel,
                    utilizationModel, utilizationModel);
            cloudlet[i].setUserId(userId);
            list.add(cloudlet[i]);
        }

        return list;
    }

    public static void main(String[] args) {
        Log.printLine("Starting CustomCloudSimExample...");

        try {
            // First step: Initialize the CloudSim package. It should be called
            // before creating any entities.
            var num_user = 2;   // number of grid users
            var calendar = Calendar.getInstance();
            var trace_flag = false;  // mean trace events

            // Initialize the CloudSim library
            CloudSim.init(num_user, calendar, trace_flag);

            var globalBroker = new CloudSimExample8.GlobalBroker("GlobalBroker");

            // Second step: Create Datacenters
            //Datacenters are the resource providers in CloudSim. We need at list one of them to run a CloudSim simulation
            @SuppressWarnings("unused")
            var datacenter0 = createDatacenter("Datacenter_0");
            @SuppressWarnings("unused")
            var datacenter1 = createDatacenter("Datacenter_1");

            //Third step: Create Broker
            var broker = createBroker("Broker0");
            assert broker != null;
            int brokerId = broker.getId();

            //Fourth step: Create VMs and Cloudlets and send them to broker
            vmlist = createVM(brokerId, 4, 0); //creating 4 vms
            cloudletList = createCloudlet(brokerId, 10, 0); // creating 10 cloudlets

            broker.submitVmList(vmlist);
            broker.submitCloudletList(cloudletList);

            CloudSim.startSimulation();

            var newList = broker.getCloudletReceivedList();
            newList.addAll(globalBroker.getBroker().getCloudletReceivedList());

            CloudSim.stopSimulation();

            printCloudletList(newList);

            Log.printLine("CustomCloudSimExample finished!");
        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("The simulation has been terminated due to an unexpected error");
        }
    }

    private static Datacenter createDatacenter(String name) {
        var hostList = new ArrayList<Host>();
        var peList1 = new ArrayList<Pe>();

        int mips = 1000;

        peList1.add(new Pe(0, new PeProvisionerSimple(mips)));
        peList1.add(new Pe(1, new PeProvisionerSimple(mips)));

        int hostId = 0;
        int ram = 500; //host memory (MB)
        var storage = 1000000; //host storage
        int bw = 10000;

        hostList.add(
                new Host(
                        hostId++,
                        new RamProvisionerSimple(ram),
                        new BwProvisionerSimple(bw),
                        storage,
                        peList1,
                        new VmSchedulerTimeShared(peList1)
                )
        );

        hostList.add(
                new Host(
                        hostId,
                        new RamProvisionerSimple(ram),
                        new BwProvisionerSimple(bw),
                        storage,
                        peList1,
                        new VmSchedulerSpaceShared(peList1)
                )
        );

        var arch = "x86";      // system architecture
        var os = "Linux";          // operating system
        var vmm = "Xen";
        var time_zone = 10.0;         // time zone this resource located
        var cost = 3.0;              // the cost of using processing in this resource
        var costPerMem = 0.05;        // the cost of using memory in this resource
        var costPerStorage = 0.1;    // the cost of using storage in this resource
        var costPerBw = 0.1;            // the cost of using bw in this resource
        var storageList = new LinkedList<Storage>();    //we are not adding SAN devices by now

        var characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);


        Datacenter datacenter = null;
        try {
            datacenter = new Datacenter(name, characteristics,
                    new VmAllocationPolicySimple(hostList), storageList, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return datacenter;
    }

    private static DatacenterBroker createBroker(String name) {

        DatacenterBroker broker = null;
        try {
            broker = new DatacenterBroker(name);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return broker;
    }

    private static void printCloudletList(List<Cloudlet> list) {
        Cloudlet cloudlet;

        var indent = "    ";
        Log.printLine();
        Log.printLine("========== OUTPUT ==========");
        Log.printLine("Cloudlet ID" + indent + "STATUS" + indent +
                "Data center ID" + indent + "VM ID" + indent + indent + "Time" + indent +
                "Start Time" + indent + "Finish Time");

        var dft = new DecimalFormat("###.##");
        for (var value : list) {
            cloudlet = value;
            Log.print(indent + cloudlet.getCloudletId() + indent + indent);

            if (cloudlet.getStatus() == Cloudlet.SUCCESS) {
                Log.print("SUCCESS");

                Log.printLine(indent + indent + cloudlet.getResourceId() + indent + indent +
                        indent + cloudlet.getVmId() + indent + indent + indent +
                        dft.format(cloudlet.getActualCPUTime()) + indent + indent +
                        dft.format(cloudlet.getExecStartTime()) + indent + indent + indent +
                        dft.format(cloudlet.getFinishTime()));
            }
        }
    }
}