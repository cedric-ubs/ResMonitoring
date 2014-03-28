package fr.univubs.labsticc.resmonitoring;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.StringTokenizer;

/**
 *
 * 1 récupérer les ressources informatiques dans l'ontologie, les rentrer dans
 * un tableau avec leur adresse ip A terme, il pourrait y avoir plsusieurs types
 * de ressources dont les variables monitorées seraient différentes avec une
 * fréquence perso ie res-infos, res-domos(portes, lampes, etc), etc 2 On ping
 * les ressources pour s'assurer qu'elles sont toujours dispos 3 En ssh on
 * obtient les variables informatiques 4 M-a-j de l'onto (suppression des res
 * inaccessible et modif des autres)
 *
 */
public class ResMonitApp {

    static byte resNumber = 3;

    /* This is the ontology */
    private static final String ONTOLOGY = "/home/cedric/ontologies/examples/exp_lampes_rdf.owl";
    private final Path fFilePath;

    public static void main(String[] args) throws UnknownHostException, IOException {

        ResMonitApp object = new ResMonitApp(ONTOLOGY);
        String[][] AvailableResTab = initAvailableResTab(object.fFilePath);
        /* Are resources reachable ? */
        areResStillAvail(AvailableResTab);

    }

    private ResMonitApp(String aFileName) {
        fFilePath = Paths.get(aFileName);
    }

    private static String[][] initAvailableResTab(Path fFilePath) {
        /* On récupère le label (nom) des ressources ainsi que leur adresse ip
         * à partir du fichier onto.owl que l'on parcourera
         */
        String[][] AvailableResTab = new String[4][3];
        /* ressource localhost */
        AvailableResTab[0][0] = "_res_Localhost";
        AvailableResTab[0][1] = "127.0.0.1";
        AvailableResTab[0][2] = "seguin";
        /* ressource workstation */
        AvailableResTab[2][0] = "_res_Pc1";
        AvailableResTab[2][1] = "192.168.100.1";
        AvailableResTab[2][2] = "seguin";
        /* ressource laptop */
        AvailableResTab[1][0] = "_res_Pc2";
        AvailableResTab[1][1] = "192.168.100.2";
        AvailableResTab[1][2] = "cedric";
        /* ressource alacon */
        AvailableResTab[3][0] = "_res_AlaCon";
        AvailableResTab[3][1] = "192.168.100.7";
        AvailableResTab[3][2] = "cedric";

        return AvailableResTab;
    }

    private static void areResStillAvail(String[][] AvailableResTab) throws UnknownHostException, IOException {
        for (int i = 0; i < AvailableResTab.length; i++) {
            InetAddress address = InetAddress.getByName(AvailableResTab[i][1]);
            if (isReachableIp(address)) {
                /* la ressource a ete atteinte */
                log("resource " + AvailableResTab[i][0]
                        + " with ip : " + AvailableResTab[i][1] + " reachable");
                getCpuUsage(AvailableResTab, i);
                getMemUsage(AvailableResTab, i);
            } else {
                /* la ressource n'a ete atteinte */
                log("resource " + AvailableResTab[i][0]
                        + " with ip : " + AvailableResTab[i][1] + " unreachable");
            }
        }
    }

    private static float getCpuUsage(String[][] AvailableResTab, int res_number) throws IOException {
        String line = "";
        /* creation de la commande a executer */
        String cmd = "ssh " + AvailableResTab[res_number][2]
                + "@" + AvailableResTab[res_number][1] + " cat /proc/loadavg";
        /* execution de la commande */
        line = RuntimeCmd(cmd);
        String[] loadavg = new String[3];
        StringTokenizer st = new StringTokenizer(line);
        for(int i=0;i<3;i++){
            loadavg[i] = st.nextToken();
        }
        float fCpu = Float.parseFloat(loadavg[0]);
        log("CPUavg = "+fCpu);
        return fCpu;
    }

    private static float getMemUsage(String[][] AvailableResTab, int res_number) throws IOException {
        String line = "";
        /* creation de la commande a executer */
        String cmd = "ssh " + AvailableResTab[res_number][2]
                + "@" + AvailableResTab[res_number][1] + " free -mt | grep Total";
        /* execution de la commande */
        line = RuntimeCmd(cmd);
        String[] memusage = new String[3];
        StringTokenizer st = new StringTokenizer(line);
        for(int i=0;i<3;i++){
            memusage[i] = st.nextToken();
        }
        float TotalMem = Float.parseFloat(memusage[1]);
                log("TotalMem = "+TotalMem);
        float UsedMem = Float.parseFloat(memusage[2]);
                log("UsedMem = "+UsedMem);
        float fMem = UsedMem/TotalMem;
        log("MEMavg = "+fMem);
        return fMem;
    }

    private static boolean isReachableIp(InetAddress address) throws IOException {

        if (address.isReachable(5)) {   // milliseconde
            //System.out.println(address + " machine is turned on and can be pinged");
            return true;
        } else if (!address.getHostAddress().equals(address.getHostName())) {
            //System.out.println(address + " machine is known in a DNS lookup");
            return false;
        } else {
            //System.out.println(address + " Destination Host Unreachable");
            return false;
        }
    }

    private static String RuntimeCmd(String command) throws IOException {
        Runtime runtime = Runtime.getRuntime();
        final Process process = runtime.exec(command);
        // Consommation de la sortie standard de l'application externe
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line = "";
        while ((line = reader.readLine()) != null) {
            // Traitement du flux de sortie de l'application si besoin est
            //log(line);
            break;
        }
        return line;
    }

    private static void log(Object aObject) {
        System.out.println(String.valueOf(aObject));
    }
}
