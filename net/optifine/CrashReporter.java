package net.optifine;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.src.OFConfig;
import net.optifine.http.FileUploadThread;
import net.optifine.http.IFileUploadListener;
import net.optifine.shaders.Shaders;

public class CrashReporter
{
    public static void onCrashReport(CrashReport crashReport, CrashReportCategory category)
    {
        try
        {
            Throwable throwable = crashReport.getCrashCause();

            if (throwable == null)
            {
                return;
            }

            if (throwable.getClass().getName().contains(".fml.client.SplashProgress"))
            {
                return;
            }

            if (throwable.getClass() == Throwable.class)
            {
                return;
            }

            extendCrashReport(category);
            GameSettings gamesettings = OFConfig.getGameSettings();

            if (gamesettings == null)
            {
                return;
            }

            if (!gamesettings.snooperEnabled)
            {
                return;
            }

            String s = "http://optifine.net/crashReport";
            String s1 = makeReport(crashReport);
            byte[] abyte = s1.getBytes("ASCII");
            IFileUploadListener ifileuploadlistener = new IFileUploadListener()
            {
                public void fileUploadFinished(String url, byte[] content, Throwable exception)
                {
                }
            };
            Map map = new HashMap();
            map.put("OF-Version", OFConfig.getVersion());
            map.put("OF-Summary", makeSummary(crashReport));
            FileUploadThread fileuploadthread = new FileUploadThread(s, map, abyte, ifileuploadlistener);
            fileuploadthread.setPriority(10);
            fileuploadthread.start();
            Thread.sleep(1000L);
        }
        catch (Exception exception)
        {
            OFConfig.dbg(exception.getClass().getName() + ": " + exception.getMessage());
        }
    }

    private static String makeReport(CrashReport crashReport)
    {
        StringBuffer stringbuffer = new StringBuffer();
        stringbuffer.append("OptiFineVersion: " + OFConfig.getVersion() + "\n");
        stringbuffer.append("Summary: " + makeSummary(crashReport) + "\n");
        stringbuffer.append("\n");
        stringbuffer.append(crashReport.getCompleteReport());
        stringbuffer.append("\n");
        return stringbuffer.toString();
    }

    private static String makeSummary(CrashReport crashReport)
    {
        Throwable throwable = crashReport.getCrashCause();

        if (throwable == null)
        {
            return "Unknown";
        }
        else
        {
            StackTraceElement[] astacktraceelement = throwable.getStackTrace();
            String s = "unknown";

            if (astacktraceelement.length > 0)
            {
                s = astacktraceelement[0].toString().trim();
            }

            String s1 = throwable.getClass().getName() + ": " + throwable.getMessage() + " (" + crashReport.getDescription() + ")" + " [" + s + "]";
            return s1;
        }
    }

    public static void extendCrashReport(CrashReportCategory cat)
    {
        cat.addCrashSection("OptiFine Version", OFConfig.getVersion());
        cat.addCrashSection("OptiFine Build", OFConfig.getBuild());

        if (OFConfig.getGameSettings() != null)
        {
            cat.addCrashSection("Render Distance Chunks", "" + OFConfig.getChunkViewDistance());
            cat.addCrashSection("Mipmaps", "" + OFConfig.getMipmapLevels());
            cat.addCrashSection("Anisotropic Filtering", "" + OFConfig.getAnisotropicFilterLevel());
            cat.addCrashSection("Antialiasing", "" + OFConfig.getAntialiasingLevel());
            cat.addCrashSection("Multitexture", "" + OFConfig.isMultiTexture());
        }

        cat.addCrashSection("Shaders", "" + Shaders.getShaderPackName());
        cat.addCrashSection("OpenGlVersion", "" + OFConfig.openGlVersion);
        cat.addCrashSection("OpenGlRenderer", "" + OFConfig.openGlRenderer);
        cat.addCrashSection("OpenGlVendor", "" + OFConfig.openGlVendor);
        cat.addCrashSection("CpuCount", "" + OFConfig.getAvailableProcessors());
    }
}
