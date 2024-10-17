import java.io.*;

public class Main {
    public static void main(String[] args) {
        String fileName = "data.txt";
        String resultFileName = "result.txt";

        try (BufferedReader br = new BufferedReader(new FileReader(fileName));
             BufferedWriter fw = new BufferedWriter(new FileWriter(resultFileName)))
        {
            String line = br.readLine();

            while (line != null) {
                if (line.startsWith("#")) {
                    line = br.readLine();
                    continue;
                }

                String[] values = line.split(",");

                if (values.length != 3) {
                    System.out.println("Error: Invalid input");
                }

                int T_preferred = Integer.parseInt(values[0]);
                int T_Outcar = Integer.parseInt(values[1]);
                int T_Incar = Integer.parseInt(values[2]);

                ClimateControlResult result = ClimateControlFunc(T_Incar, T_Outcar, T_preferred);

                String output = String.format("%d, %d, %d -> %d, %d, %s, %s\n", T_preferred, T_Outcar, T_Incar, result.K_deflector, result.K_fan, result.airflowDirection, (result.AC_on_off ? "Включен" : "Выключен"));

                fw.write(output);

                line = br.readLine();
            }
        }
        catch (IOException e)
        {
            System.out.println("An error occurred while reading the file.");
            e.printStackTrace();
        }
    }

    public static class ClimateControlResult {
        int K_deflector;
        int K_fan;
        String airflowDirection;
        boolean AC_on_off;

        ClimateControlResult(int K_deflector, int K_fan, String airflowDirection, boolean AC_on_off) {
            this.K_deflector = K_deflector;
            this.K_fan = K_fan;
            this.airflowDirection = airflowDirection;
            this.AC_on_off = AC_on_off;
        }

    }

    public static ClimateControlResult ClimateControlFunc(int T_Incar, int T_Outcar, int T_preferred) {
        int K_deflector = 0;
        int K_fan = 0;
        boolean AC_on_off;
        String Airflow_Direction = "";
        int T_max = 29;
        int T_min = 10;
        double alpha = 0.5;

        if(T_Incar > T_preferred)
        {
            if(T_Outcar < 16) {
                AC_on_off = false;
            }
            else{
                AC_on_off = true;
            }
            Airflow_Direction = "In car";
            if(T_Incar > T_max){
                K_deflector = 0;
                K_fan = 100;
            }
            else {
                K_fan = (int)(((Math.abs(T_Incar - T_preferred) + alpha * Math.abs(T_Outcar - T_Incar)) / T_max) * 100);
                K_deflector = 50 -  K_fan;
                if(T_Outcar > 25){
                    K_deflector = 0;
                }
            }
        }
        else {
            if (T_Outcar > 20) {
                AC_on_off = false;
                Airflow_Direction = "In car";
                K_fan = (int) (((Math.abs(T_Incar - T_preferred) + alpha * Math.abs(T_Outcar - T_Incar)) / T_max) * 100);
                K_deflector = 50 - K_fan;
            } else {
                AC_on_off = false;
                Airflow_Direction = "In car and on glass";
                if (T_Incar < T_min) {
                    K_fan = 100;
                    K_deflector = 100;
                } else {
                    K_fan = (int) (((Math.abs(T_Incar - T_preferred) + alpha * Math.abs(T_Outcar - T_Incar)) / T_max) * 100);
                    K_deflector = 50 + K_fan;
                }
            }
        }

        if (K_fan < 20){
            K_fan = 20;
        }
        if (T_Outcar < 0 && K_deflector < 15) {
            K_deflector = 15;
        }
        if (K_fan > 100){
            K_fan = 100;
        }
        if (K_deflector > 100){
            K_deflector = 100;
        }
        if (K_deflector < 0){
            K_deflector = 0;
        }

        return new ClimateControlResult(K_deflector, K_fan, Airflow_Direction, AC_on_off);
    }
}
