import javax.swing.JOptionPane;

public class ModelSettings {

	private static ModelSettings instance = new ModelSettings();
	private Anonym anonym;
	private Synchroned synchroned;

	public static ModelSettings getInstance() {
		return instance;
	}

	private ModelSettings() {
		this.anonym = Anonym.anonymOff;
		this.synchroned = Synchroned.synchronedOff;
	}

	public void setAnonym(Anonym anonym) {
		this.anonym = anonym;
	}

	public void setSynchroned(Synchroned synchroned) {
		this.synchroned = synchroned;
	}

	public Anonym getAnonym() {
		return anonym;
	}

	public Synchroned getSynchroned() {
		return synchroned;
	}

	public void setSettings() {
		Dialog.DialogProgramSettings newProgramSettings = new Dialog.DialogProgramSettings();
		int ok = JOptionPane.showConfirmDialog(null,
				newProgramSettings.getPanel(), "Program settings",
				JOptionPane.OK_CANCEL_OPTION);
		if (ok != JOptionPane.OK_OPTION)
			return;
		if (anonym == Anonym.anonymOff && newProgramSettings.getAnonym()) {
			anonym = Anonym.anonymOn;
			AnonymModifier visitor = new AnonymModifier();
			GUI.graph.accept(visitor);
		}
		if (anonym == Anonym.anonymOn && !newProgramSettings.getAnonym()) {
			anonym = Anonym.anonymOff;
			UnanonymModifier visitor = new UnanonymModifier();
			GUI.graph.accept(visitor);
		}
	}

}