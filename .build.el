;; project settings
(setq ent-project-home (file-name-directory (if load-file-name load-file-name buffer-file-name)))
(setq ent-project-name "powder-example")
(setq ent-clean-regexp "~$\\|\\.tex$")
(setq ent-project-config-filename "Powder-Example.org")

;; local functions

(defvar project-version)

(setq project-version (ent-get-version))

(setq ent-cli '(("templates/dev.cli" . "powder-example.sh")
                ("templates/prod.cli" ."deploy/powder-example.sh")
                ("templates/powder-exampled.cli" ."deploy/powder-exampled")
                ("templates/dist.cli" . "deploy/dist.sh")
                ("templates/update.cli" . "deploy/update.sh")
                ))

(setq pack-update '("deploy/powder-example.sh"
                    "deploy/update.sh"
                    "deploy/powder-exampled"))

(setq pack-dist '("deploy/powder-example.sh"
                  "deploy/start_powder-example.sh"
                  "deploy/stop_powder-example.sh"
                  "deploy/status_powder-example.sh"
                  "deploy/dist.sh"
                  "deploy/powder-exampled"))

(defun add-file-to-zip (zip filename)
  (progn
    (message "add %s to %s" filename zipfile)
    (call-process "zip" nil t t "-j" zipfile filename)))

(defun get-package-name ()
  (concat "target/" ent-project-name "-" project-version "-standalone.jar"))

(defun make-pack (zipfile pack)
  (let ((zip (expand-file-name zipfile ent-project-home)))
    (if (file-exists-p zipfile) (delete-file zip))
    (mapc (function (lambda (x) (add-file-to-zip zip x))) pack)
    (add-file-to-zip zip (get-package-name))
    (call-process "shasum" nil t t zipfile)))

(defun make-update-pack () (make-pack "powder-example_update.zip" pack-update))

(defun make-dist-pack () (make-pack "powder-example.zip" pack-dist))


;; tasks

(load ent-init-file)

(task 'doc '(compile) "build the project documentation" '(lambda (&optional x) "lein doc"))

(task 'format '() "format the project" '(lambda (&optional x) "lein cljfmt fix")) 

(task 'check '() "check the project" '(lambda (&optional x) "lein with-profile +check checkall"))

(task 'tree '() "tree dependencies" '(lambda (&optional x) "lein do clean, deps :tree"))

(task 'tests '() "run tests" '(lambda (&optional x) "lein  with-profile +dan do clean, test"))

(task 'libupdate () "update project libraries" '(lambda (&optional x) "lein ancient :no-colors"))

(task 'package '() "package the library" '(lambda (&optional x) "lein do clean, uberjar"))

(task 'gencli '() "generate script files" '(lambda (&optional x) (ent-emacs "ent-make-all-cli-files"
                                                                            (expand-file-name ent-file-name ent-project-home))))

(task 'deploy '(gencli package) "deploy the application" '(lambda (&optional x) "chmod a+x deploy/qserv*;ls -l deploy"))

(task 'distupdate '(deploy) "make update distribution" '(lambda (&optional x) (ent-emacs "make-update-pack" 
                                                                                         (expand-file-name ent-file-name ent-project-home))))

(task 'dist '(deploy) "make distribution" '(lambda (&optional x) (ent-emacs "make-dist-pack" 
                                                                            (expand-file-name ent-file-name ent-project-home))))



;; Local Variables:
;; no-byte-compile: t
;; no-update-autoloads: t
;; End:
