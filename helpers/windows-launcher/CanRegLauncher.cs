using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Text;
using System.Windows.Forms;

namespace CanRegLauncher
{
    internal static class Program
    {
        [STAThread]
        private static int Main(string[] args)
        {
            string baseDir = AppDomain.CurrentDomain.BaseDirectory.TrimEnd(
                Path.DirectorySeparatorChar, Path.AltDirectorySeparatorChar);

            try
            {
                Environment.CurrentDirectory = baseDir;
            }
            catch (Exception)
            {
                // Continue even if setting current directory fails; ProcessStartInfo also sets WorkingDirectory.
            }

            string classpath = ResolveClasspath(baseDir);
            if (classpath == null)
            {
                MessageBox.Show(
                    "Could not find CanReg.jar in:\n" + baseDir + "\n\nPlease reinstall CanReg5 or build with 'ant jar'.",
                    "CanReg5 Launcher Error",
                    MessageBoxButtons.OK,
                    MessageBoxIcon.Error);
                return 1;
            }

            string javawPath = FindJavaw(baseDir);
            if (javawPath == null)
            {
                MessageBox.Show(
                    "Unable to locate a Java 25+ runtime (javaw.exe).\n\n"
                    + "Please install Java 25 or later, or use the CanReg5-Setup installer which includes a bundled Java runtime.",
                    "CanReg5 - Java Runtime Not Found",
                    MessageBoxButtons.OK,
                    MessageBoxIcon.Error);
                return 1;
            }

            StringBuilder arguments = new StringBuilder();
            arguments.Append("-Xms256m -Xmx1024m -cp ");
            arguments.Append(QuoteArgument(classpath));
            arguments.Append(" canreg.client.CanRegClientApp");
            if (args != null)
            {
                foreach (string arg in args)
                {
                    arguments.Append(' ');
                    arguments.Append(QuoteArgument(arg));
                }
            }

            try
            {
                ProcessStartInfo startInfo = new ProcessStartInfo();
                startInfo.FileName = javawPath;
                startInfo.Arguments = arguments.ToString();
                startInfo.WorkingDirectory = baseDir;
                startInfo.UseShellExecute = false;

                Process.Start(startInfo);
                return 0;
            }
            catch (Exception ex)
            {
                MessageBox.Show(
                    "Failed to launch CanReg5 using:\n" + javawPath + "\n\nError: " + ex.Message,
                    "CanReg5 Launcher Error",
                    MessageBoxButtons.OK,
                    MessageBoxIcon.Error);
                return 1;
            }
        }

        private static string ResolveClasspath(string baseDir)
        {
            string rootJar = Path.Combine(baseDir, "CanReg.jar");
            if (File.Exists(rootJar))
            {
                return "CanReg.jar;lib/*";
            }

            string distJar = Path.Combine(baseDir, Path.Combine("dist", "CanReg.jar"));
            if (File.Exists(distJar))
            {
                return "dist\\CanReg.jar;dist\\lib/*";
            }

            return null;
        }

        private static string FindJavaw(string baseDir)
        {
            // 1. Bundled jlink runtime (CanReg5-Setup / CanReg5-with-R-Setup) and alternate runtime folders
            string[] bundledCandidates = new string[]
            {
                Path.Combine(baseDir, Path.Combine("jre", Path.Combine("bin", "javaw.exe"))),
                Path.Combine(baseDir, Path.Combine("dist", Path.Combine("jre", Path.Combine("bin", "javaw.exe")))),
                Path.Combine(baseDir, Path.Combine("runtime", Path.Combine("bin", "javaw.exe")))
            };
            foreach (string candidate in bundledCandidates)
            {
                if (File.Exists(candidate))
                {
                    return candidate;
                }
            }

            // 2. JAVA_HOME environment variable
            string javaHome = Environment.GetEnvironmentVariable("JAVA_HOME");
            if (!string.IsNullOrEmpty(javaHome))
            {
                string trimmedHome = javaHome.Trim().Trim('"');
                string javaHomeCandidate = Path.Combine(trimmedHome, Path.Combine("bin", "javaw.exe"));
                if (File.Exists(javaHomeCandidate))
                {
                    return javaHomeCandidate;
                }
            }

            // 3. Common 64-bit JDK installation directories (prefer highest version name)
            string programFiles = Environment.GetEnvironmentVariable("ProgramFiles");
            if (!string.IsNullOrEmpty(programFiles))
            {
                string[] vendorRoots = new string[]
                {
                    Path.Combine(programFiles, "Java"),
                    Path.Combine(programFiles, "Eclipse Adoptium"),
                    Path.Combine(programFiles, "Zulu"),
                    Path.Combine(programFiles, "Microsoft")
                };

                List<string> discovered = new List<string>();
                foreach (string vendorRoot in vendorRoots)
                {
                    if (!Directory.Exists(vendorRoot))
                    {
                        continue;
                    }
                    try
                    {
                        string[] dirs = Directory.GetDirectories(vendorRoot);
                        Array.Sort(dirs, StringComparer.OrdinalIgnoreCase);
                        Array.Reverse(dirs);
                        foreach (string dir in dirs)
                        {
                            string candidate = Path.Combine(dir, Path.Combine("bin", "javaw.exe"));
                            if (File.Exists(candidate))
                            {
                                discovered.Add(candidate);
                            }
                        }
                    }
                    catch (Exception)
                    {
                        // Ignore inaccessible directories and keep searching.
                    }
                }

                string preferred = PickHighestJavaCandidate(discovered);
                if (preferred != null)
                {
                    return preferred;
                }
            }

            // 4. javaw.exe on PATH
            string pathEnv = Environment.GetEnvironmentVariable("PATH");
            if (!string.IsNullOrEmpty(pathEnv))
            {
                foreach (string entry in pathEnv.Split(';'))
                {
                    if (string.IsNullOrEmpty(entry))
                    {
                        continue;
                    }
                    string dir = entry.Trim().Trim('"');
                    if (dir.Length == 0)
                    {
                        continue;
                    }
                    try
                    {
                        string candidate = Path.Combine(dir, "javaw.exe");
                        if (File.Exists(candidate))
                        {
                            return candidate;
                        }
                    }
                    catch (Exception)
                    {
                        // Skip invalid PATH entries.
                    }
                }
            }

            return null;
        }

        private static string PickHighestJavaCandidate(List<string> candidates)
        {
            if (candidates == null || candidates.Count == 0)
            {
                return null;
            }
            // Prefer explicit Java 25+ directory names first, then fall back to first descending entry.
            foreach (string candidate in candidates)
            {
                string dirName = Path.GetFileName(Path.GetDirectoryName(Path.GetDirectoryName(candidate)));
                if (!string.IsNullOrEmpty(dirName) && dirName.Contains("25"))
                {
                    return candidate;
                }
            }
            return candidates[0];
        }

        private static string QuoteArgument(string arg)
        {
            if (arg == null)
            {
                return "\"\"";
            }
            if (arg.Length > 0 && arg.IndexOfAny(new char[] { ' ', '\t', '"' }) == -1)
            {
                return arg;
            }
            StringBuilder sb = new StringBuilder();
            sb.Append('"');
            int backslashes = 0;
            for (int i = 0; i < arg.Length; i++)
            {
                char c = arg[i];
                if (c == '\\')
                {
                    backslashes++;
                }
                else if (c == '"')
                {
                    sb.Append('\\', backslashes * 2 + 1);
                    sb.Append('"');
                    backslashes = 0;
                }
                else
                {
                    if (backslashes > 0)
                    {
                        sb.Append('\\', backslashes);
                        backslashes = 0;
                    }
                    sb.Append(c);
                }
            }
            if (backslashes > 0)
            {
                sb.Append('\\', backslashes * 2);
            }
            sb.Append('"');
            return sb.ToString();
        }
    }
}
