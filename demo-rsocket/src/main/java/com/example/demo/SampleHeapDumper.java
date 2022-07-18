package com.example.demo;

import java.io.File;
import java.nio.file.StandardOpenOption;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.util.MimeTypeUtils;

import socktuator.api.SocktuatorClient;

//@Component
public class SampleHeapDumper {

	private static final Logger log = LoggerFactory.getLogger(SampleHeapDumper.class);

	@Autowired
	SocktuatorClient client;
	
	@EventListener(ApplicationReadyEvent.class)
	void dumpit() throws Exception {
		File dumpFile = new File("sample-heapdump.hprof").getAbsoluteFile();
		DataBufferUtils.write(client.callForBytes("heapdump.dump", MimeTypeUtils.APPLICATION_OCTET_STREAM, Map.of()),
				dumpFile.toPath(),
				StandardOpenOption.CREATE
		)
		.thenReturn("ok")
		.subscribe(
			success -> {
				log.info("Heapdump written to {}", dumpFile);
			}, 
			error -> {
				error.printStackTrace();
			}
		);
	}


}
