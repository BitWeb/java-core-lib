import json
import re
from collections import defaultdict
from packaging.version import Version
from xml.etree import ElementTree as ET
import urllib.request

# Configuration
managed_minors = {"3.0", "3.1", "3.2", "3.3", "3.4"}
boot_java_compatibility = {
    "3.0": ["17"],           # Java 21 not supported
    "3.1": ["17", "21"],
    "3.2": ["17", "21"],
    "3.3": ["17", "21"],
    "3.4": ["17", "21"]
}
output_path = ".github/spring-versions.json"

# Step 1: Fetch Spring Boot versions
metadata_url = "https://repo1.maven.org/maven2/org/springframework/boot/spring-boot-starter/maven-metadata.xml"
with urllib.request.urlopen(metadata_url) as response:
    xml_data = response.read()

root = ET.fromstring(xml_data)
versions = [v.text for v in root.findall(".//version")]

boot_versions = defaultdict(list)
for v in versions:
    if not re.match(r"^\d+\.\d+\.\d+$", v):
        continue
    minor = ".".join(v.split(".")[:2])
    if minor in managed_minors:
        boot_versions[minor].append(Version(v))

latest_boot_versions = {minor: str(max(vlist)) for minor, vlist in boot_versions.items()}

# Step 2: Resolve Spring Framework version
def get_spring_framework_version(boot_version: str) -> str:
    pom_url = f"https://repo1.maven.org/maven2/org/springframework/boot/spring-boot-starter/{boot_version}/spring-boot-starter-{boot_version}.pom"
    try:
        with urllib.request.urlopen(pom_url) as response:
            pom_data = response.read()
        pom_root = ET.fromstring(pom_data)
        ns = {'m': 'http://maven.apache.org/POM/4.0.0'}
        for dep in pom_root.findall(".//m:dependency", ns):
            gid = dep.find("m:groupId", ns)
            aid = dep.find("m:artifactId", ns)
            ver = dep.find("m:version", ns)
            if gid is not None and aid is not None and ver is not None:
                if gid.text == "org.springframework" and aid.text == "spring-core":
                    return ver.text
    except Exception as e:
        print(f"Warning: Failed to fetch framework version for {boot_version}: {e}")
    return "unknown"

# Step 3: Build matrix
matrix_entries = []
for minor, boot_version in sorted(latest_boot_versions.items()):
    framework_version = get_spring_framework_version(boot_version)
    for java_version in boot_java_compatibility.get(minor, []):
        matrix_entries.append({
            "boot": boot_version,
            "framework": framework_version,
            "java": str(java_version)
        })

# Step 4: Save result
with open(output_path, "w") as f:
    json.dump({"matrix": matrix_entries}, f, indent=2)

print(f"Updated {output_path} with {len(matrix_entries)} matrix entries.")
