package io.slot;

import org.apache.maven.model.Build;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * 插槽建造器
 *
 * @author Payne 646742615@qq.com
 * 2019/2/16 11:25
 */
@Mojo(name = "transform", defaultPhase = LifecyclePhase.PACKAGE)
public class SlotTransformer extends AbstractMojo {
    /**
     * 当前Maven工程
     */
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    /**
     * 原本JAR所在文件夹
     */
    @Parameter(property = "slot.sourceDir", required = true, defaultValue = "${project.build.directory}")
    private File sourceDir;

    /**
     * 原本JAR名称
     */
    @Parameter(property = "slot.sourceJar", required = true, defaultValue = "${project.build.finalName}.jar")
    private String sourceJar;

    /**
     * 生成JAR所在文件夹
     */
    @Parameter(property = "slot.targetDir", required = true, defaultValue = "${project.build.directory}")
    private File targetDir;

    /**
     * 生成JAR名称
     */
    @Parameter(property = "slot.targetJar", required = true, defaultValue = "${project.build.finalName}-slot.jar")
    private String targetJar;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Log log = getLog();
        String packaging = project.getPackaging();
        if (!"jar".equalsIgnoreCase(packaging)) {
            throw new MojoExecutionException("Could not transform project with packaging: " + packaging);
        }

        Build build = project.getBuild();
        Map<String, Plugin> plugins = build.getPluginsAsMap();
        Plugin plugin = plugins.get("org.springframework.boot:spring-boot-maven-plugin");
        // 非Spring-Boot项目/模块
        if (plugin == null) {
            throw new MojoFailureException("Could not transform non-spring-boot project");
        }
        // Spring-Boot项目/模块
        else {
            Object configuration = plugin.getConfiguration();
            // 不允许开启 <executable>true<executable>
            if (configuration instanceof Xpp3Dom) {
                Xpp3Dom dom = (Xpp3Dom) configuration;
                {
                    Xpp3Dom child = dom.getChild("executable");
                    String executable = child != null ? child.getValue() : null;
                    if ("true".equalsIgnoreCase(executable)) {
                        String msg = "Unsupported to transform <executable>true</executable> spring boot JAR file, ";
                        msg += "maybe you should upgrade slot-maven-plugin dependency if it have been supported in the later versions,";
                        msg += "if not, delete <executable>true</executable> or set executable as false for the configuration of spring-boot-maven-plugin.";
                        throw new MojoFailureException(msg);
                    }
                }
                {
                    Xpp3Dom child = dom.getChild("embeddedLaunchScript");
                    String embeddedLaunchScript = child != null ? child.getValue() : null;
                    if (embeddedLaunchScript != null) {
                        String msg = "Unsupported to transform <embeddedLaunchScript>...</embeddedLaunchScript> spring boot JAR file, ";
                        msg += "maybe you should upgrade slot-maven-plugin dependency if it have been supported in the later versions,";
                        msg += "if not, delete <embeddedLaunchScript>...</embeddedLaunchScript> for the configuration of spring-boot-maven-plugin.";
                        throw new MojoFailureException(msg);
                    }
                }
            }
        }

        try {
            File src = new File(sourceDir, sourceJar);
            File dest = new File(targetDir, targetJar);
            File folder = dest.getParentFile();
            if (!folder.exists() && !folder.mkdirs() && !folder.exists()) {
                throw new IOException("could not make directory: " + folder);
            }

            log.info("Transforming " + src + " to " + dest);

            Slotter slotter = new BootSlotter();
            slotter.slot(src, dest);
        } catch (IOException e) {
            throw new MojoFailureException(e.getMessage(), e);
        }
    }

    public MavenProject getProject() {
        return project;
    }

    public void setProject(MavenProject project) {
        this.project = project;
    }

    public File getSourceDir() {
        return sourceDir;
    }

    public void setSourceDir(File sourceDir) {
        this.sourceDir = sourceDir;
    }

    public String getSourceJar() {
        return sourceJar;
    }

    public void setSourceJar(String sourceJar) {
        this.sourceJar = sourceJar;
    }

    public File getTargetDir() {
        return targetDir;
    }

    public void setTargetDir(File targetDir) {
        this.targetDir = targetDir;
    }

    public String getTargetJar() {
        return targetJar;
    }

    public void setTargetJar(String targetJar) {
        this.targetJar = targetJar;
    }
}
