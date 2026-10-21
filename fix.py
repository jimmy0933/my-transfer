with open('CameraTranslator/app/build.gradle.kts', 'r') as f:
    text = f.read()

text = text.replace('\\$camerax_version', '$camerax_version')
text = text.replace('\\$room_version', '$room_version')

with open('CameraTranslator/app/build.gradle.kts', 'w') as f:
    f.write(text)
