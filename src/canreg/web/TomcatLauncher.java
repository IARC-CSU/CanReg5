package canreg.web;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;

import javax.servlet.ServletException;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TomcatLauncher {

    private static ExecutorService executor = Executors.newSingleThreadExecutor();
    private static Thread thread;

    public static void main(String[] args) {
        TomcatLauncher tl = new TomcatLauncher();
        tl.launch(8081);
    }

    public void shutdown() {
        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void launch(final int webPort) {
        System.setProperty("file.encoding", "UTF-8");
        if (thread != null)
            thread.interrupt();
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Tomcat tomcat = new Tomcat();
                String webappDirLocation = "web/";

                tomcat.setPort(webPort);

                try {
                    tomcat.addWebapp("/", new File(webappDirLocation).getAbsolutePath());
                    tomcat.getConnector().setURIEncoding("UTF-8");
                    System.out.println("configuring app with basedir: " + new File("./" + webappDirLocation).getAbsolutePath());

                    tomcat.start();
                    tomcat.getServer().await();
                } catch (ServletException e) {
                    e.printStackTrace();
                } catch (LifecycleException e) {
                    e.printStackTrace();
                }
            }
        });

        executor.execute(thread);

    }
}
