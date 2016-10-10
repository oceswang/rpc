package com.galaxy.rpc.common.util;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ClassUtil
{
	private static final String CLASS_EXT = ".class";
	private static final String JAR_EXT = ".jar";
	private static final String DOT = ".";
	private static final String ZIP_SLASH = "/";
	private static final String BLACK = "";

	public static List<Class<?>> scan(String packageName, ClassFilter filter)
	{
		String[] classPathArray = getClassPathArray();
		List<Class<?>> classes = new ArrayList<>();
		for (String classPath : classPathArray)
		{
			fillClasses(new File(classPath), packageName, filter, classes);
		}
		return classes;
	}

	private static void fillClasses(File file, String packageName, ClassFilter filter, List<Class<?>> classes)
	{
		if (isDir(file))
		{
			processDir(file, packageName, filter, classes);
		} else if (isExtMatch(file, JAR_EXT))
		{
			processJar(file, packageName, filter, classes);
		} else if (isExtMatch(file, CLASS_EXT))
		{
			processClassFile(file, packageName, filter, classes);
		}
	}

	private static void fillClass(String className, String packageName, List<Class<?>> classes, ClassFilter filter)
	{
		if (className.indexOf(packageName) == 0)
		{
			try
			{
				final Class<?> clazz = Class.forName(className, Boolean.FALSE, ClassUtil.class.getClassLoader());
				if (filter != null && filter.accept(clazz))
				{
					classes.add(clazz);
				}
			} catch (Throwable ex)
			{
				// ignore this ex
			}
		}
	}

	private static void processDir(File file, String packageName, ClassFilter filter, List<Class<?>> classes)
	{
		for (File f : file.listFiles())
		{
			fillClasses(f, packageName, filter, classes);
		}
	}

	private static void processJar(File file, String packageName, ClassFilter filter, List<Class<?>> classes)
	{
		try
		{
			for (ZipEntry entry : Collections.list(new ZipFile(file).entries()))
			{
				if (isExtMatch(entry.getName(), JAR_EXT))
				{
					final String className = entry.getName().replace(ZIP_SLASH, DOT).replace(CLASS_EXT, BLACK);
					fillClass(className, packageName, classes, filter);
				}
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private static void processClassFile(File file, String packageName, ClassFilter classFilter, List<Class<?>> classes)
	{
		final String filePathWithDot = file.getAbsolutePath().replace(File.separator, DOT);
		int subIndex = -1;
		if ((subIndex = filePathWithDot.indexOf(packageName)) != -1)
		{
			final String className = filePathWithDot.substring(subIndex).replace(CLASS_EXT, BLACK);
			fillClass(className, packageName, classes, classFilter);
		}
	}

	public static String[] getClassPathArray()
	{
		return System.getProperty("java.class.path").split(System.getProperty("path.separator"));
	}

	private static boolean isDir(File file)
	{
		return file.isDirectory();
	}

	private static boolean isExtMatch(File file, String ext)
	{
		return file.getName().endsWith(ext);
	}

	private static boolean isExtMatch(String fileName, String ext)
	{
		return fileName.endsWith(ext);
	}
	
	public static void main(String args[])
	{
		List<Class<?>> classes = scan("com",new ClassFilter(){

			@Override
			public boolean accept(Class<?> clazz)
			{
				Annotation[] anns = clazz.getAnnotations();
				return anns != null && anns.length>0;
			}
		});
		System.out.println(classes);
	}
}