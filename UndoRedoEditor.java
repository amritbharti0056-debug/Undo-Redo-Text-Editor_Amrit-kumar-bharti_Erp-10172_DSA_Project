import java.io.*;
import java.util.Scanner;

/** 
 * Undo/Redo Text Editor (PS No. 17)
 * Document  : singly linked list of lines
 * Undo/Redo : two stacks (custom linked stacks)
 * Storage   : document.txt (loaded at start, saved on exit)
 */
public class UndoRedoEditor {

    // ---------- Linked list node holding one line ----------
    static class LineNode {
        String text;
        LineNode next;
        LineNode(String text) { this.text = text; }
    }

    // ---------- One recorded change ----------
    static class Action {
        final boolean added;   // true = a line was added, false = a line was deleted
        final String text;
        Action(boolean added, String text) { this.added = added; this.text = text; }
    }

    // ---------- Generic LIFO stack (linked nodes) ----------
    static class ActionStack {
        private static class Node {
            Action a;
            Node below;
            Node(Action a, Node below) { this.a = a; this.below = below; }
        }
        private Node top;

        void push(Action a) { top = new Node(a, top); }

        Action pop() {
            if (top == null) return null;
            Action a = top.a;
            top = top.below;
            return a;
        }

        boolean isEmpty() { return top == null; }
        void clear() { top = null; }
    }

    // ---------- Document: linked list of lines ----------
    static class Document {
        private LineNode head, tail;
        private int size;

        void addLine(String text) {
            LineNode n = new LineNode(text);
            if (head == null) head = tail = n;
            else { tail.next = n; tail = n; }
            size++;
        }

        String removeLastLine() {
            if (head == null) return null;
            String removed;
            if (head == tail) {
                removed = head.text;
                head = tail = null;
            } else {
                LineNode cur = head;
                while (cur.next != tail) cur = cur.next;
                removed = tail.text;
                cur.next = null;
                tail = cur;
            }
            size--;
            return removed;
        }

        boolean isEmpty() { return size == 0; }

        String render() {
            if (head == null) return "Document is empty.";
            StringBuilder sb = new StringBuilder("Document:");
            int i = 1;
            for (LineNode c = head; c != null; c = c.next, i++)
                sb.append(i == 1 ? " " : "  ").append(i).append(". ").append(c.text);
            return sb.toString();
        }

        void saveToFile(String fileName) throws IOException {
            try (PrintWriter pw = new PrintWriter(new FileWriter(fileName))) {
                for (LineNode c = head; c != null; c = c.next) pw.println(c.text);
            }
        }

        void loadFromFile(String fileName) throws IOException {
            File f = new File(fileName);
            if (!f.exists()) return;
            try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                String s;
                while ((s = br.readLine()) != null) addLine(s);
            }
        }
    }

    // ---------- Editor: methods for each feature ----------
    private final Document doc = new Document();
    private final ActionStack undoStack = new ActionStack();
    private final ActionStack redoStack = new ActionStack();
    private static final String FILE = "document.txt";

    void addLine(String text) {
        doc.addLine(text);
        undoStack.push(new Action(true, text));
        redoStack.clear();                       // a new change clears redo
        System.out.println("Line added.");
        System.out.println(doc.render());
    }

    void deleteLastLine() {
        String removed = doc.removeLastLine();
        if (removed == null) { System.out.println("Nothing to delete."); return; }
        undoStack.push(new Action(false, removed));
        redoStack.clear();                       // a new change clears redo
        System.out.println("Last line deleted.");
        System.out.println(doc.render());
    }

    void undo() {
        if (undoStack.isEmpty()) { System.out.println("Nothing to undo."); return; }
        Action a = undoStack.pop();
        if (a.added) doc.removeLastLine(); else doc.addLine(a.text);
        redoStack.push(a);
        System.out.println("Undo done. " + doc.render());
    }

    void redo() {
        if (redoStack.isEmpty()) { System.out.println("Nothing to redo."); return; }
        Action a = redoStack.pop();
        if (a.added) doc.addLine(a.text); else doc.removeLastLine();
        undoStack.push(a);
        System.out.println("Redo done. " + doc.render());
    }

    void display() { System.out.println(doc.render()); }

    void run() {
        try { doc.loadFromFile(FILE); }
        catch (IOException e) { System.out.println("Could not load file: " + e.getMessage()); }

        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("\n1 Add line  2 Delete last line  3 Undo  4 Redo  5 Display  6 Exit");
            System.out.print("Enter choice: ");
            if (!sc.hasNextLine()) break;
            String in = sc.nextLine().trim();
            int ch;
            try { ch = Integer.parseInt(in); }
            catch (NumberFormatException e) { ch = -1; }

            switch (ch) {
                case 1: System.out.print("Text: "); addLine(sc.hasNextLine() ? sc.nextLine() : ""); break;
                case 2: deleteLastLine(); break;
                case 3: undo(); break;
                case 4: redo(); break;
                case 5: display(); break;
                case 6:
                    try { doc.saveToFile(FILE); System.out.println("Saved to " + FILE + ". Goodbye!"); }
                    catch (IOException e) { System.out.println("Could not save file: " + e.getMessage()); }
                    return;
                default: System.out.println("Invalid option. Choose 1 to 6.");
            }
        }
    }

    public static void main(String[] args) { new UndoRedoEditor().run(); }
} 
