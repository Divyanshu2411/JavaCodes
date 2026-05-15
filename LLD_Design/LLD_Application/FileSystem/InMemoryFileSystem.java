package LLD_Application.FileSystem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 1. COMPOSITE PATTERN: Base Component
abstract class Node {
    String name;
    Directory parent;

    public Node(String name, Directory parent) {
        this.name = name;
        this.parent = parent;
    }

    public abstract boolean isFile();

    // Your new abstract method
    public abstract void delete();
}

// 2. COMPOSITE PATTERN: Leaf
class File extends Node {
    String content = "";

    public File(String name, Directory parent) {
        super(name, parent);
    }

    @Override
    public boolean isFile() {
        return true;
    }

    @Override
    public void delete() {
        if (this.parent != null) {
            this.parent.children.remove(this.name);
        }
    }
}

// 3. COMPOSITE PATTERN: Composite
class Directory extends Node {
    Map<String, Node> children = new HashMap<>();

    public Directory(String name, Directory parent) {
        super(name, parent);
    }

    @Override
    public boolean isFile() {
        return false;
    }

    @Override
    public void delete() {
        // 1. We must make a copy of the values to iterate over.
        // If we iterate directly on the map while children are calling
        // `parent.children.remove()`, Java will throw a ConcurrentModificationException!
        List<Node> childrenList = new ArrayList<>(this.children.values());

        // 2. Recursively tell all children to delete themselves
        for (Node child : childrenList) {
            child.delete();
        }

        // 3. Finally, unlink THIS directory from its parent
        if (this.parent != null) {
            this.parent.children.remove(this.name);
        }
    }
}

// 4. FACADE: Main FileSystem Class
class FileSystem {
    private Directory root;
    private Directory current;

    public FileSystem() {
        root = new Directory("", null);
        current = root;
    }

    // --- Core Navigation & Resolution ---

    private Node resolve(String path) {
        if (path == null || path.equals("/")) return root;
        Node curr = path.startsWith("/") ? root : current;
        String[] parts = path.split("/");

        for (int i = 0; i < parts.length; i++) {
            String p = parts[i];
            if (p.isEmpty() || p.equals(".")) continue;

            if (p.equals("..")) {
                if (curr.parent != null) curr = curr.parent;
            } else if (!curr.isFile()) {
                curr = ((Directory) curr).children.get(p);
                if (curr == null) return null;
            } else {
                return null; // Invalid path (trying to enter a file)
            }
        }
        return curr;
    }

    private String getParentPath(String path) {
        int idx = path.lastIndexOf("/");
        if (idx == -1) return ".";
        if (idx == 0) return "/";
        return path.substring(0, idx);
    }

    private String getName(String path) {
        String[] parts = path.split("/");
        return parts[parts.length - 1];
    }

    // --- Public API ---

    public void cd(String path) {
        Node node = resolve(path);
        if (node != null && !node.isFile()) {
            current = (Directory) node;
        }
    }

    public String pwd() {
        Node temp = current;
        StringBuilder sb = new StringBuilder();
        while (temp != null && !temp.name.isEmpty()) {
            sb.insert(0, "/" + temp.name);
            temp = temp.parent;
        }
        return sb.length() == 0 ? "/" : sb.toString();
    }

    // Creates missing intermediate directories automatically
    public void mkdir(String path) {
        if (path == null || path.isEmpty() || path.equals("/")) return;

        Node curr = path.startsWith("/") ? root : current;
        String[] parts = path.split("/");

        for (int i = 0; i < parts.length; i++) {
            String p = parts[i];
            if (p.isEmpty() || p.equals(".")) continue;

            if (p.equals("..")) {
                if (curr.parent != null) curr = curr.parent;
            } else if (!curr.isFile()) {
                Directory dir = (Directory) curr;
                if (!dir.children.containsKey(p)) {
                    dir.children.put(p, new Directory(p, dir));
                }
                curr = dir.children.get(p);
            } else {
                return; // Invalid path through a file
            }
        }
    }

    public void touch(String path) {
        Node parentNode = resolve(getParentPath(path));
        if (parentNode != null && !parentNode.isFile()) {
            String name = getName(path);
            ((Directory) parentNode).children.put(name, new File(name, (Directory) parentNode));
        }
    }

    public List<String> ls(String path) {
        Node node = resolve(path);
        List<String> result = new ArrayList<>();

        if (node == null) {
            return result;
        }

        if (node.isFile()) {
            result.add(node.name);
        } else {
            for (String key : ((Directory) node).children.keySet()) {
                result.add(key);
            }
            Collections.sort(result); // Lexicographical sort without streams
        }
        return result;
    }

    public void delete(String path) {
        Node node = resolve(path);
        if (node != null && node != root) {
            node.delete(); // Polymorphism handles the rest!
        }
    }

    public void move(String sourcePath, String destDirPath) {
        Node src = resolve(sourcePath);
        Node destDir = resolve(destDirPath);

        if (src != null && src != root && destDir != null && !destDir.isFile()) {
            src.parent.children.remove(src.name);
            src.parent = (Directory) destDir;
            ((Directory) destDir).children.put(src.name, src);
        }
    }

    public void addContent(String path, String content) {
        Node node = resolve(path);
        if (node != null && node.isFile()) {
            ((File) node).content += content;
        }
    }

    public String readContent(String path) {
        Node node = resolve(path);
        if (node != null && node.isFile()) {
            return ((File) node).content;
        }
        return "";
    }
}

public class InMemoryFileSystem {
        public static void main(String[] args) {
            FileSystem fs = new FileSystem();

            System.out.println("=========================================");
            System.out.println("   INITIALIZING FILE SYSTEM SIMULATION   ");
            System.out.println("=========================================\n");

            System.out.println("[System] Booting up...");
            System.out.println("Current Path: " + fs.pwd());

            // Step 1: Setting up a workspace
            System.out.println("\n[Action] Creating developer workspace and directories...");
            fs.mkdir("/workspace/project/src");
            fs.mkdir("/workspace/project/docs");
            System.out.println("Workspace created.");

            // Step 2: Navigating the tree
            System.out.println("\n[Action] Navigating to the source directory...");
            fs.cd("/workspace/project/src");
            System.out.println("Current Path: " + fs.pwd());

            // Step 3: Creating files
            System.out.println("\n[Action] Creating source files and documentation...");
            fs.touch("App.java");
            fs.touch("Config.java");

            // Creating a file in the parent directory using relative paths
            fs.touch("../readme.txt");

            System.out.println("Contents of 'src': " + fs.ls(fs.pwd()));
            System.out.println("Contents of 'project' root: " + fs.ls(".."));

            // Step 4: Simulating file I/O
            System.out.println("\n[Action] Developer is writing code to App.java...");
            fs.addContent("App.java", "public class App {\n");
            fs.addContent("App.java", "    public static void main(String[] args) {\n");
            fs.addContent("App.java", "        System.out.println(\"Hello System Design!\");\n");
            fs.addContent("App.java", "    }\n}");

            System.out.println("\n--- App.java Output ---");
            System.out.print(fs.readContent("App.java"));
            System.out.println("-----------------------");

            // Step 5: Reorganizing the file system
            System.out.println("\n[Action] Oops, readme.txt belongs in the docs folder. Moving it now...");
            fs.move("/workspace/project/readme.txt", "/workspace/project/docs");

            System.out.println("Contents of 'project' root: " + fs.ls("/workspace/project"));
            System.out.println("Contents of 'docs' folder: " + fs.ls("/workspace/project/docs"));

            // Step 6: Recursive cleanup
            System.out.println("\n[Action] Project complete. Deleting the entire workspace...");
            fs.delete("/workspace");

            System.out.println("Contents of root (/): " + fs.ls("/"));

            System.out.println("\n=========================================");
            System.out.println("         SIMULATION COMPLETE             ");
            System.out.println("=========================================");
        }
}
