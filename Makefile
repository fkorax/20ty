
LOCAL_BIN_DIR = ${HOME}/.local/bin
INSTALL_DIR = $(LOCAL_BIN_DIR)/20ty
LOCAL_APP_DIR = ${HOME}/.local/share/applications
AUTOSTART_DIR = ${HOME}/.config/autostart

# TODO In the future, the version should be generated
# by Gradle and read from there.
APP_VERSION = 1.0-SNAPSHOT

BUILD_DIRECTORY = build
FAT_JAR_FILE = 20ty-all-$(APP_VERSION).jar

APP_EXEC_FILE = $(INSTALL_DIR)/20ty

define APP_EXEC_CONTENT
#!/bin/sh\n\
\n\
java -jar $(FAT_JAR_FILE)\n
endef

APP_DESKTOP_FILE = 20ty.desktop

define APP_DESKTOP_CONTENT
[Desktop Entry]\n\
Version=1.0\n\
Name=20ty\n\
Comment=20-20-20 to you\n\
Keywords=twenty\n\
Icon=$(INSTALL_DIR)/icon.png\n\
Type=Application\n\
Terminal=false\n\
Path=$(INSTALL_DIR)\n\
TryExec=$(INSTALL_DIR)/20ty\n\
Exec=$(INSTALL_DIR)/20ty
endef

run:
	./gradlew run

build:
	./gradlew fatJar

$(INSTALL_DIR):
	mkdir -p $@

install: build $(INSTALL_DIR)
	# Ensure the install directories are available
	cp $(BUILD_DIRECTORY)/libs/$(FAT_JAR_FILE) $(INSTALL_DIR)/$(FAT_JAR_FILE)
	# Generate the executive file
	printf "$(subst \n ,\n,$(APP_EXEC_CONTENT))" > $(APP_EXEC_FILE)
	chmod +x $(APP_EXEC_FILE)
	# Copy the icon
	cp images/icon.png $(INSTALL_DIR)/icon.png
	# Generate the desktop file, both in applications/ and autostart/ directories
	printf "$(subst \n ,\n,$(APP_DESKTOP_CONTENT))" | tee $(LOCAL_APP_DIR)/$(APP_DESKTOP_FILE) $(AUTOSTART_DIR)/$(APP_DESKTOP_FILE)

uninstall:
	rm -r $(INSTALL_DIR)
	rm $(LOCAL_APP_DIR)/$(APP_DESKTOP_FILE)
	rm $(AUTOSTART_DIR)/$(APP_DESKTOP_FILE)

clean:
	./gradlew clean

.PHONY: clean build run install uninstall
