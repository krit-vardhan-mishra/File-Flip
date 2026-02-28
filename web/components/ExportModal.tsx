import { useState } from 'react';
import { motion, AnimatePresence } from 'motion/react';
import { 
  FileText, 
  FileCode2, 
  Code, 
  Download, 
  Loader2,
  X
} from 'lucide-react';

interface ExportModalProps {
  isOpen: boolean;
  onClose: () => void;
  contentId?: string;
  fileName?: string;
}

export default function ExportModal({ isOpen, onClose, contentId, fileName = 'document' }: ExportModalProps) {
  const [format, setFormat] = useState('pdf');
  const [template, setTemplate] = useState('github-dark');
  const [isGenerating, setIsGenerating] = useState(false);

  const handleExport = async () => {
    setIsGenerating(true);
    
    try {
      if (format === 'pdf' && contentId) {
        const element = document.getElementById(contentId);
        if (element) {
          // Dynamically import html2pdf to avoid SSR issues
          const html2pdf = (await import('html2pdf.js')).default;
          
          const opt = {
            margin:       10,
            filename:     `${fileName.split('.')[0]}.pdf`,
            image:        { type: 'jpeg' as const, quality: 0.98 },
            html2canvas:  { scale: 2, useCORS: true },
            jsPDF:        { unit: 'mm' as const, format: 'a4', orientation: 'portrait' as const }
          };
          
          await html2pdf().set(opt).from(element).save();
        }
      } else {
        // Handle other text-based formats
        let contentToExport = '';
        let mimeType = 'text/plain';
        let extension = format;

        if (contentId) {
            const element = document.getElementById(contentId);
            if (format === 'html') {
                contentToExport = element?.innerHTML || '';
                mimeType = 'text/html';
            } else {
                // For MD and TXT, we might want the raw text if available, 
                // but since we only have the rendered HTML in the preview, 
                // we'll extract text content for TXT. For MD, ideally we'd pass the raw markdown.
                // As a fallback, we'll just use innerText.
                contentToExport = element?.innerText || '';
                if (format === 'md') {
                    mimeType = 'text/markdown';
                }
            }
        }

        const blob = new Blob([contentToExport], { type: mimeType });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `${fileName.split('.')[0]}.${extension}`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        URL.revokeObjectURL(url);
      }
    } catch (error) {
      console.error('Export failed:', error);
    } finally {
      setIsGenerating(false);
      onClose();
    }
  };

  return (
    <AnimatePresence>
      {isOpen && (
        <motion.div 
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          exit={{ opacity: 0 }}
          className="fixed inset-0 bg-black/40 backdrop-blur-sm z-50 flex items-center justify-center p-4"
        >
          <motion.div 
            initial={{ scale: 0.95, opacity: 0, y: 20 }}
            animate={{ scale: 1, opacity: 1, y: 0 }}
            exit={{ scale: 0.95, opacity: 0, y: 20 }}
            transition={{ type: "spring", damping: 25, stiffness: 300 }}
            className="bg-md-sys-color-surface w-full max-w-[560px] rounded-[24px] shadow-2xl flex flex-col relative overflow-hidden ring-1 ring-md-sys-color-outline-variant"
          >
        
        {/* Header */}
        <div className="px-6 pt-6 pb-4 flex flex-col gap-1">
          <div className="flex items-center justify-between mb-4">
            <div className="flex items-center justify-center w-12 h-1 bg-md-sys-color-outline-variant rounded-full self-center md:hidden"></div>
            <button onClick={onClose} className="absolute top-6 right-6 text-md-sys-color-on-surface-variant hover:text-md-sys-color-on-surface">
              <X className="w-6 h-6" />
            </button>
          </div>
          <Download className="text-md-sys-color-primary w-8 h-8 mb-2" />
          <h2 className="text-2xl text-md-sys-color-on-surface font-normal">Export File</h2>
          <p className="text-md-sys-color-on-surface-variant text-sm">Choose your preferred format and styling options.</p>
        </div>

        {/* Content */}
        <div className="px-6 overflow-y-auto max-h-[60vh] flex flex-col gap-8 mb-6 custom-scrollbar">
          
          {/* Format Selection */}
          <div className="flex flex-col gap-3">
            <label className="text-sm font-medium text-md-sys-color-primary uppercase tracking-wide">Format</label>
            <div className="flex flex-wrap gap-2">
              <button 
                onClick={() => setFormat('pdf')}
                className={`flex items-center h-8 px-4 rounded-lg border text-sm font-medium transition-colors duration-200 select-none ${
                  format === 'pdf' 
                    ? 'bg-md-sys-color-primary/10 text-md-sys-color-primary border-transparent ring-1 ring-md-sys-color-primary ring-offset-1 ring-offset-md-sys-color-surface' 
                    : 'border-md-sys-color-outline text-md-sys-color-on-surface-variant hover:bg-md-sys-color-surface-variant'
                }`}
              >
                <FileText className="w-4 h-4 mr-2" />
                PDF Document
              </button>
              <button 
                onClick={() => setFormat('md')}
                className={`flex items-center h-8 px-4 rounded-lg border text-sm font-medium transition-colors duration-200 select-none ${
                  format === 'md' 
                    ? 'bg-md-sys-color-primary/10 text-md-sys-color-primary border-transparent ring-1 ring-md-sys-color-primary ring-offset-1 ring-offset-md-sys-color-surface' 
                    : 'border-md-sys-color-outline text-md-sys-color-on-surface-variant hover:bg-md-sys-color-surface-variant'
                }`}
              >
                <FileCode2 className="w-4 h-4 mr-2" />
                Markdown (.md)
              </button>
              <button 
                onClick={() => setFormat('txt')}
                className={`flex items-center h-8 px-4 rounded-lg border text-sm font-medium transition-colors duration-200 select-none ${
                  format === 'txt' 
                    ? 'bg-md-sys-color-primary/10 text-md-sys-color-primary border-transparent ring-1 ring-md-sys-color-primary ring-offset-1 ring-offset-md-sys-color-surface' 
                    : 'border-md-sys-color-outline text-md-sys-color-on-surface-variant hover:bg-md-sys-color-surface-variant'
                }`}
              >
                <FileText className="w-4 h-4 mr-2" />
                Plain Text (.txt)
              </button>
              <button 
                onClick={() => setFormat('html')}
                className={`flex items-center h-8 px-4 rounded-lg border text-sm font-medium transition-colors duration-200 select-none ${
                  format === 'html' 
                    ? 'bg-md-sys-color-primary/10 text-md-sys-color-primary border-transparent ring-1 ring-md-sys-color-primary ring-offset-1 ring-offset-md-sys-color-surface' 
                    : 'border-md-sys-color-outline text-md-sys-color-on-surface-variant hover:bg-md-sys-color-surface-variant'
                }`}
              >
                <Code className="w-4 h-4 mr-2" />
                HTML
              </button>
            </div>
          </div>

          {/* Template Style */}
          <div className="flex flex-col gap-3">
            <label className="text-sm font-medium text-md-sys-color-primary uppercase tracking-wide">Template Style</label>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
              
              <div 
                onClick={() => setTemplate('github-dark')}
                className={`relative flex flex-col items-start p-4 rounded-2xl border cursor-pointer transition-all duration-200 ${
                  template === 'github-dark'
                    ? 'border-md-sys-color-primary bg-md-sys-color-primary/10'
                    : 'border-md-sys-color-outline-variant bg-md-sys-color-surface hover:bg-md-sys-color-surface-variant'
                }`}
              >
                <div className="flex items-center justify-between w-full mb-2">
                  <span className="text-md-sys-color-on-surface font-medium text-sm">GitHub Dark</span>
                  <div className={`w-5 h-5 rounded-full border-2 flex items-center justify-center mr-3 ${template === 'github-dark' ? 'border-md-sys-color-primary' : 'border-md-sys-color-on-surface-variant'}`}>
                    <div className={`w-2.5 h-2.5 rounded-full bg-md-sys-color-primary transition-transform duration-200 ${template === 'github-dark' ? 'scale-100' : 'scale-0'}`}></div>
                  </div>
                </div>
                <div className="w-full h-16 bg-[#0d1117] rounded border border-[#30363d] p-2 overflow-hidden opacity-80">
                  <div className="w-2/3 h-2 bg-[#58a6ff] rounded-sm mb-2"></div>
                  <div className="w-full h-1 bg-[#8b949e] rounded-sm mb-1"></div>
                  <div className="w-4/5 h-1 bg-[#8b949e] rounded-sm mb-1"></div>
                </div>
              </div>

              <div 
                onClick={() => setTemplate('classic-light')}
                className={`relative flex flex-col items-start p-4 rounded-2xl border cursor-pointer transition-all duration-200 ${
                  template === 'classic-light'
                    ? 'border-md-sys-color-primary bg-md-sys-color-primary/10'
                    : 'border-md-sys-color-outline-variant bg-md-sys-color-surface hover:bg-md-sys-color-surface-variant'
                }`}
              >
                <div className="flex items-center justify-between w-full mb-2">
                  <span className="text-md-sys-color-on-surface font-medium text-sm">Classic Light</span>
                  <div className={`w-5 h-5 rounded-full border-2 flex items-center justify-center mr-3 ${template === 'classic-light' ? 'border-md-sys-color-primary' : 'border-md-sys-color-on-surface-variant'}`}>
                    <div className={`w-2.5 h-2.5 rounded-full bg-md-sys-color-primary transition-transform duration-200 ${template === 'classic-light' ? 'scale-100' : 'scale-0'}`}></div>
                  </div>
                </div>
                <div className="w-full h-16 bg-white rounded border border-gray-200 p-2 overflow-hidden opacity-80">
                  <div className="w-2/3 h-2 bg-gray-800 rounded-sm mb-2"></div>
                  <div className="w-full h-1 bg-gray-400 rounded-sm mb-1"></div>
                  <div className="w-4/5 h-1 bg-gray-400 rounded-sm mb-1"></div>
                </div>
              </div>

            </div>
          </div>

          {/* Generating State */}
          {isGenerating && (
            <div className="flex items-center gap-4 p-4 rounded-xl bg-md-sys-color-surface-variant bg-opacity-30 border border-md-sys-color-outline-variant">
              <Loader2 className="w-6 h-6 text-md-sys-color-primary animate-spin" />
              <div className="flex flex-col">
                <span className="text-sm text-md-sys-color-on-surface font-medium">Generating preview...</span>
                <span className="text-xs text-md-sys-color-on-surface-variant">Calculating page breaks</span>
              </div>
            </div>
          )}

        </div>

        {/* Footer */}
        <div className="p-6 pt-2 flex justify-end gap-2 border-t border-md-sys-color-outline-variant bg-md-sys-color-surface">
          <button 
            onClick={onClose}
            className="h-10 px-6 rounded-full text-md-sys-color-primary font-medium text-sm hover:bg-md-sys-color-primary/10 transition-colors"
          >
            Cancel
          </button>
          <button 
            onClick={handleExport}
            disabled={isGenerating}
            className="h-10 px-6 rounded-full bg-md-sys-color-primary text-md-sys-color-on-primary font-medium text-sm hover:shadow-lg hover:bg-md-sys-color-primary/90 transition-all flex items-center gap-2 disabled:opacity-70 disabled:cursor-not-allowed"
          >
            <Download className="w-4 h-4" />
            Export
          </button>
        </div>

          </motion.div>
        </motion.div>
      )}
    </AnimatePresence>
  );
}
