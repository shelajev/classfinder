package com.poolik.classfinder;

import com.poolik.classfinder.info.ClassInfo;
import com.poolik.classfinder.info.FileUtil;
import com.poolik.classfinder.io.*;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.zip.ZipEntryCallback;
import org.zeroturnaround.zip.ZipUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;

public class ParallelClassLoader implements ClassLoader {
  private static final Logger log = LoggerFactory.getLogger(ParallelClassLoader.class);

  @Override
  public Map<String, ClassInfo> loadClassesFrom(Collection<File> placesToSearch) {
    HashMap<String, ClassInfo> foundClasses = new HashMap<>();
    for (File file : placesToSearch) {
      loadClassesIn(file, foundClasses);
    }
    return foundClasses;
  }

  private void loadClassesIn(File file, Map<String, ClassInfo> foundClasses) {
    String name = file.getPath();
    log.info("Finding classes in " + name);
    if (FileUtil.isJar(name) || FileUtil.isZip(name))
      processZip(name, foundClasses);
    else
      processDirectory(file, foundClasses);
  }


  private void processZip(final String zipName,
                          final Map<String, ClassInfo> foundClasses) {
    final File zip = new File(zipName);
    ZipUtil.iterate(zip, new ZipEntryCallback() {
      public void process(InputStream in, ZipEntry zipEntry) throws IOException {
        if ((!zipEntry.isDirectory()) && (zipEntry.getName().endsWith(".class"))) {
          try {
            log.trace("Loading " + zipName + "(" + zipEntry.getName() + ")");
            loadClassData(in, new ClassInfoClassVisitor(foundClasses, zip));
          } catch (ClassFinderException ex) {
            log.error("Can't open \"" + zipEntry.getName() + "\" in file \"" + zipName + "\": ", ex);
          }
        }
      }
    });
  }

  private void processDirectory(File dir, Map<String, ClassInfo> foundClasses) {
    loadAllClassFilesInDir(dir, foundClasses);
    loadAllJarFilesInDir(dir, foundClasses);
    loadAllZipFilesInDir(dir, foundClasses);
  }

  private void loadAllZipFilesInDir(File dir, Map<String, ClassInfo> foundClasses) {
    loadAllFilesWithSuffixInDir(dir, ".zip", foundClasses);
  }

  private void loadAllJarFilesInDir(File dir, Map<String, ClassInfo> foundClasses) {
    loadAllFilesWithSuffixInDir(dir, ".jar", foundClasses);
  }

  private void loadAllFilesWithSuffixInDir(File dir, String suffix, Map<String, ClassInfo> foundClasses) {
    for (File file : filterFilesBySuffix(dir, suffix)) {
      loadClassesIn(file, foundClasses);
    }
  }

  private void loadAllClassFilesInDir(File dir, Map<String, ClassInfo> foundClasses) {
    for (File classFile : filterFilesBySuffix(dir, ".class")) {
      String path = classFile.getPath();
      log.trace("Loading " + classFile.getPath());
      try (InputStream is = new FileInputStream(classFile)) {
        loadClassData(is, new ClassInfoClassVisitor(foundClasses, dir));
      } catch (IOException | ClassFinderException ex) {
        log.error("Can't open '" + path + "': ", ex);
      }
    }
  }

  private Collection<File> filterFilesBySuffix(File dir, String suffix) {
    try {
      return DirUtils.findWithSuffix(dir.toPath(), suffix);
    } catch (IOException e) {
      log.error("Failed to load files with suffix '" + suffix +"' ", e);
      return new ArrayList<>();
    }
  }

  private void loadClassData(InputStream is, ClassVisitor classVisitor)
      throws ClassFinderException {
    try {
      ClassReader cr = new ClassReader(is);
      cr.accept(classVisitor, ClassInfo.ASM_CR_ACCEPT_CRITERIA);
    } catch (Exception ex) {
      throw new ClassFinderException("Unable to load class from open input stream", ex);
    }
  }
}