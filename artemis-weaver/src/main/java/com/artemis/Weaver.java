package com.artemis;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;

import com.artemis.meta.ClassMetadata;
import com.artemis.meta.ClassMetadata.WeaverType;
import com.artemis.meta.MetaScanner;
import com.artemis.weaver.ComponentTypeTransmuter;

public class Weaver {
	public static final String PACKED_ANNOTATION = "Lcom/artemis/annotations/PackedWeaver;";
	public static final String POOLED_ANNOTATION = "Lcom/artemis/annotations/PooledWeaver;";
	public static final String WOVEN_ANNOTATION = "Lcom/artemis/annotations/internal/Transmuted";
	
	private final File targetClasses;
	
	
	public Weaver(File outputDirectory) {
		this.targetClasses = outputDirectory;
	}
	
	public static void main(String[] args)
	{
		ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		List<ClassMetadata> processed = new ArrayList<ClassMetadata>();
		if (args.length == 0) {
			for (File f : ClassUtil.find(".")) {
				processClass(threadPool, f.getAbsolutePath(), processed);
			}
		} else {
			for (String arg : args) {
				// eclipse sends folders along too
				if (arg.endsWith(".class")) processClass(threadPool, arg, processed);
			}
		}
		
		awaitTermination(threadPool);
	}

	public List<ClassMetadata> execute() {
		ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		List<ClassMetadata> processed = new ArrayList<ClassMetadata>();
		for (File f : ClassUtil.find(targetClasses))
			processClass(threadPool, f.getAbsolutePath(), processed);
		
		awaitTermination(threadPool);
		return processed;
	}

	private static void processClass(ExecutorService threadPool, String file, List<ClassMetadata> processed) {
		
		ClassReader cr = classReaderFor(file);
		ClassMetadata meta = scan(cr);
		
		if (meta.annotation == WeaverType.NONE)
			return;

		threadPool.submit(new ComponentTypeTransmuter(file, cr, meta));
		processed.add(meta);
	}
	
	static ClassReader classReaderFor(InputStream file) {
		try {
			return new ClassReader(file);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	static ClassReader classReaderFor(String file) {
		FileInputStream stream = null;
		try
		{
			stream = new FileInputStream(file);
			return classReaderFor(stream);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} finally {
			if (stream != null) try {
				stream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static ClassMetadata scan(ClassReader source) {
		ClassMetadata info = new ClassMetadata();
		source.accept(new MetaScanner(info), 0);
		info.type = Type.getObjectType(source.getClassName());
		return info;
	}
	
	private static void awaitTermination(ExecutorService threadPool) {
		threadPool.shutdown();
		try {
			threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
