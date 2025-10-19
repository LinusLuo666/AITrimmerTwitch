package com.aitrimmertwitch.repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aitrimmertwitch.model.VideoEditTask;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FileTaskRepository implements TaskRepository {

	private static final Logger log = LoggerFactory.getLogger(FileTaskRepository.class);

	private final Path storageFile;

	private final ObjectMapper mapper;

	private final ReadWriteLock lock = new ReentrantReadWriteLock();

	private final Map<UUID, VideoEditTask> store = new LinkedHashMap<>();

	public FileTaskRepository(Path storageFile, ObjectMapper mapper) {
		this.storageFile = storageFile;
		this.mapper = mapper;
		load();
	}

	@Override
	public VideoEditTask save(VideoEditTask task) {
		lock.writeLock().lock();
		try {
			store.put(task.getId(), task);
			persist();
			return task;
		}
		finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	public Optional<VideoEditTask> findById(UUID id) {
		lock.readLock().lock();
		try {
			return Optional.ofNullable(store.get(id));
		}
		finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public List<VideoEditTask> findAll() {
		lock.readLock().lock();
		try {
			return new ArrayList<>(store.values());
		}
		finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public void delete(UUID id) {
		lock.writeLock().lock();
		try {
			if (store.remove(id) != null) {
				persist();
			}
		}
		finally {
			lock.writeLock().unlock();
		}
	}

	private void load() {
		lock.writeLock().lock();
		try {
			if (Files.notExists(storageFile)) {
				return;
			}
			List<VideoEditTask> tasks = mapper.readValue(storageFile.toFile(), new TypeReference<>() {
			});
			for (VideoEditTask task : tasks) {
				store.put(task.getId(), task);
			}
		}
		catch (IOException ex) {
			log.warn("读取任务存档失败，将重新建立空白存储。", ex);
		}
		finally {
			lock.writeLock().unlock();
		}
	}

	private void persist() {
		try {
			Path parent = storageFile.getParent();
			if (parent != null) {
				Files.createDirectories(parent);
			}
			mapper.writerWithDefaultPrettyPrinter().writeValue(storageFile.toFile(), store.values());
		}
		catch (IOException ex) {
			log.error("写入任务存档失败：{}", storageFile, ex);
			throw new IllegalStateException("无法写入任务存档", ex);
		}
	}

}
