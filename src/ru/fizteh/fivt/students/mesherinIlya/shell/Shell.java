
import java.io.*;


public class Shell {

    static File currentDir = new File(System.getProperty("user.dir"));

    static boolean cd(String directory) {
        try {
            File newDir = new File(directory);
            
            if (!newDir.isAbsolute()) {
                newDir = new File(currentDir.getPath() + File.separator + directory);
            }
            
            if (!newDir.isDirectory()) {
                System.out.println("cd: \'" + directory + "\': No such file or directory");
                return false;
            }
            
            currentDir = newDir;
            return true;
        }
        catch (Exception e) {
            System.err.println("cd: couldn't change current directory to \'" + directory + "\'.");
            return false;
        }
    }


    static boolean mkdir(String directory) {
        try {
            File newDir = new File(directory);
            
            if (!newDir.isAbsolute()) {
                newDir = new File(currentDir.getPath() + File.separator + directory);
            }
            
            if (newDir.exists()) {
                System.err.println("mkdir: \'" + directory + "\' is already exists!");
                return false;
            }    
            
            if (!newDir.mkdir()) {
                throw new Exception();
            }
            
            return true;
        }
        catch (Exception e) {
            System.err.println("mkdir: couldn't create the directory \'" + directory + "\'.");
            return false;
        }
    }
        
    static boolean pwd() {
        try {
            System.out.println(currentDir.getCanonicalPath());
            return true;
        }
        catch (Exception e) {
            System.err.println("pwd: couldn't print the current work directory.");
            return false;
        }
        
    }

    static boolean remove(String filename) {
        try {
            File subject = new File(filename);
            if (!subject.isAbsolute()) {
                subject = new File(currentDir.getPath() + File.separator + filename);
            }
            
            if (!subject.exists()) {
                System.err.print("rm: cannot remove \'" + filename + "\': ");
                System.err.println("No such file or directory");
                return false;
            }
            
            if (subject.isDirectory()) {
                String[] entries = subject.list();
                if (entries == null) {
                    if (!subject.delete()) {
                        throw new Exception();
                    }
                    return true;
                }
                
                for (String entry : entries) {
                    File file = new File(entry);
                    remove(file.getPath());
                }
            }
            
            if (!subject.delete()) {
                throw new Exception();
            }
            return true;
        }
        catch (Exception e) {
            System.err.print("rm: couldn't remove \'" + filename + "\'.");
            return false;
        }
        
    }

    
    static boolean copy(String file, String directory) {
        try {
            File source = new File(file);
            if (!source.isAbsolute()) {
                source = new File(currentDir.getPath() + File.separator + file);
            }
            
            if (!source.exists()) {
                System.err.println("Cannot copy \'" + file + "\': No such file or directory");
                return false;
            }    
            
            File destination = new File(directory);
            if (!destination.isAbsolute()) {
                destination = new File(currentDir.getPath() + File.separator + directory);
            }

            if (!destination.isDirectory()) {
                System.err.println("Cannot copy to \'" + directory + "\': the directory doesn't exist");
                return false;
            }
            
            if (source.isDirectory()) {
                File newDestination = new File(destination + File.separator + source.getName());
                if (!newDestination.exists()) {
                    newDestination.mkdir();
                }
                
                String[] entries = source.list();
                for (String entry : entries) {
                    File newSource = new File(source + File.separator + entry);
                    if (!copy(newSource.toString(), newDestination.toString())) {
                        return false;
                    }
                }            
            }
            else {
                FileInputStream input = null;
                FileOutputStream output = null;
                try {
                    input = new FileInputStream(source);
                    output = new FileOutputStream(destination + File.separator + source.getName());
                    int count;
                    byte[] buf = new byte[16];  
                    while (true) {
                        count = input.read(buf);
                        if (count < 0) {
                            break;
                        }
                        output.write(buf, 0, count);
                    }
                }
                finally {
                    input.close();
                    output.close();
                }
            }
        }
        catch (Exception e) {
            System.err.println("Couldn't copy the file.");
            return false;
        }
        
        return true;    
    }
    
    static boolean move(String file, String directory) {
        if (!copy(file, directory) || !remove(file)) {
            return false;
        }
    
        return true;
    }
   
    static boolean dir() {
        try {
            String[] entries = currentDir.list();
            
            for (String entry : entries) {
                System.out.println(entry);
            }
        }
        catch (Exception e) {
            System.err.println("dir: couldn't execute the command.");
        }
        return true;
    }

    static boolean executeCommand(String command) {
        String[] tokens = command.split("[\\s]+");
        
        if (tokens == null) {
            return true;
        }
        
        if (tokens[0].equals("cd")) {
            if (tokens.length == 2) {
                return cd(tokens[1]);
            }
            else {
                System.err.println("Error: cd: wrong count of arguments");
                return false;
            }
        }
        else if (tokens[0].equals("pwd")) {
            if (tokens.length == 1) {
                return pwd();
            }
            else {
                System.err.println("Error: pwd: wrong count of arguments");
                return false;
            }
        } else if (tokens[0].equals("cp")) {
            if (tokens.length == 3) {
                return copy(tokens[1], tokens[2]);
            }
            else {
                System.err.println("Error: cp: wrong count of arguments");
                return false;
            }
        } else if (tokens[0].equals("mv")) {
            if (tokens.length == 3) {
                return move(tokens[1], tokens[2]);
            }
            else {
                System.err.println("Error: mv: wrong count of arguments");
                return false;
            }
        } else if (tokens[0].equals("rm")) {
            if (tokens.length == 2) {
                return remove(tokens[1]);
            }
            else {
                System.err.println("Error: rm: wrong count of arguments");
                return false;
            }
        } else if (tokens[0].equals("dir")) {
            if (tokens.length == 1) {
                return dir();
            }
            else {
                System.err.println("Error: dir: wrong count of arguments");
                return false;
            }
        } else if (tokens[0].equals("mkdir")) {
            if (tokens.length == 2) {
                return mkdir(tokens[1]);
            }
            else {
                System.err.println("Error: mkdir: wrong count of arguments");
                return false;
            }
        } else if (tokens[0].equals("exit")) {
            if (tokens.length == 1) {
                System.exit(0);
            }
            else {
                System.err.println("Error: exit: wrong count of arguments");
                return false;
            }
        } else {
            System.err.println("Unknown command!");
            return false;
        }        
        
        return true;
    }
    
    static void interactive() {
        while (true) {
            try {
                System.out.print("$ ");
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                String string = reader.readLine();
                if (string == null) {
                    continue;
                }
                
                String[] commands = string.split(";");
                for (String command: commands) {
                    if (!executeCommand(command)) {
                        break;
                    }
                }
            }
            catch (Exception e) {
                System.err.println("Fatal error. Program has been interrupted.");
                System.exit(1);
            }        
            
            
        
        }
    }
    
    
    public static void main(String[] args) {
        
        if (args.length == 0) {
            interactive();
        }
        else {
            StringBuilder builder = new StringBuilder();
            for (String s : args) {
                builder.append(s).append(" ");
            }
        
            String string = new String(builder);
            String[] commands = string.split(";");           
            for (String command : commands) {
                if (!executeCommand(command)) {
                    System.err.println("Program has been interrupted.");
                    System.exit(1);
                }
            }
        }
        
        
    
    }





}
