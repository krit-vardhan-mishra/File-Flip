export default function JsonLd() {
  const structuredData = {
    '@context': 'https://schema.org',
    '@type': 'WebApplication',
    name: 'FileFlip',
    url: 'https://file-flip-fawn.vercel.app',
    description:
      'A high-performance, offline-capable web tool for editing, previewing, and exporting Markdown, JSON, YAML, XML, HTML, CSV and plain text files. No sign-up required.',
    applicationCategory: 'UtilitiesApplication',
    operatingSystem: 'Any',
    browserRequirements: 'Requires a modern web browser',
    offers: {
      '@type': 'Offer',
      price: '0',
      priceCurrency: 'USD',
    },
    featureList: [
      'Offline file editing',
      'Live preview',
      'Syntax highlighting',
      'PDF export',
      'Markdown support',
      'JSON tree view',
      'CSV table view',
      'YAML support',
      'XML support',
      'HTML preview',
      'Drag and drop file upload',
    ],
    screenshot: 'https://file-flip-fawn.vercel.app/og-image.png',
    softwareVersion: '1.0.0',
    author: {
      '@type': 'Person',
      name: 'Krit Vardhan Mishra',
      url: 'https://github.com/krit-vardhan-mishra',
    },
    sourceOrganization: {
      '@type': 'Organization',
      name: 'FileFlip',
      url: 'https://github.com/krit-vardhan-mishra/File-Flip',
    },
  };

  return (
    <script
      type="application/ld+json"
      dangerouslySetInnerHTML={{ __html: JSON.stringify(structuredData) }}
    />
  );
}
